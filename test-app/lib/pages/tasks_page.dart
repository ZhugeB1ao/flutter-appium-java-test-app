import 'package:flutter/material.dart';

import '../models/task.dart';
import '../services/task_service.dart';

class TasksPage extends StatefulWidget {
  final TaskService taskService;
  const TasksPage({required this.taskService, super.key});

  @override
  State<TasksPage> createState() => _TasksPageState();
}

class _TasksPageState extends State<TasksPage> {
  List<Task> _tasks = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  void _load() {
    setState(() {
      _loading = true;
    });
    final tasks = widget.taskService.loadTasks();
    setState(() {
      _tasks = tasks;
      _loading = false;
    });
  }

  Future<void> _addOrEdit({Task? task}) async {
    final titleController = TextEditingController(text: task?.title ?? '');
    final descController = TextEditingController(text: task?.description ?? '');
    final formKey = GlobalKey<FormState>();

    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(task == null ? 'Thêm công việc' : 'Chỉnh sửa công việc'),
        content: Form(
          key: formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Semantics(
                label: 'task_title_field',
                child: TextFormField(
                  key: const Key('task_title_field'),
                  controller: titleController,
                  decoration: const InputDecoration(labelText: 'Tiêu đề'),
                  validator: (v) => (v == null || v.trim().isEmpty) ? 'Vui lòng nhập tiêu đề' : null,
                ),
              ),
              Semantics(
                label: 'task_desc_field',
                child: TextFormField(
                  key: const Key('task_desc_field'),
                  controller: descController,
                  decoration: const InputDecoration(labelText: 'Mô tả (tuỳ chọn)'),
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Huỷ')),
          ElevatedButton(
            onPressed: () async {
              if (!formKey.currentState!.validate()) return;
              final title = titleController.text.trim();
              final desc = descController.text.trim();
              if (task == null) {
                final newTask = Task(id: DateTime.now().millisecondsSinceEpoch, title: title, description: desc.isEmpty ? null : desc);
                await widget.taskService.addTask(newTask);
              } else {
                task.title = title;
                task.description = desc.isEmpty ? null : desc;
                await widget.taskService.updateTask(task);
              }
              Navigator.of(context).pop(true);
            },
            child: const Text('Lưu'),
          ),
        ],
      ),
    );

    if (result == true) _load();
  }

  Future<void> _delete(int id) async {
    await widget.taskService.deleteTask(id);
    _load();
  }

  Future<void> _toggleDone(int id) async {
    await widget.taskService.toggleDone(id);
    _load();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());

    if (_tasks.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Semantics(
                label: 'empty_tasks_text',
                child: const Text('Không có công việc nào', style: TextStyle(fontSize: 18)),
              ),
              const SizedBox(height: 12),
              Semantics(
                label: 'add_task_button',
                child: ElevatedButton.icon(
                  key: const Key('add_task_button'),
                  onPressed: () => _addOrEdit(),
                  icon: const Icon(Icons.add),
                  label: const Text('Thêm công việc'),
                ),
              ),
            ],
          ),
        ),
      );
    }

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
          child: Align(
            alignment: Alignment.centerRight,
            child: Semantics(
              label: 'add_task_button',
              child: ElevatedButton.icon(
                key: const Key('add_task_button'),
                onPressed: () => _addOrEdit(),
                icon: const Icon(Icons.add),
                label: const Text('Thêm công việc'),
              ),
            ),
          ),
        ),
        const SizedBox(height: 4),
        Expanded(
          child: ListView.builder(
            itemCount: _tasks.length,
            itemBuilder: (context, index) {
              final t = _tasks[index];
              return Dismissible(
                key: ValueKey(t.id),
                direction: DismissDirection.endToStart,
                background: Container(
                  color: Colors.red,
                  alignment: Alignment.centerRight,
                  padding: const EdgeInsets.symmetric(horizontal: 16.0),
                  child: const Icon(Icons.delete, color: Colors.white),
                ),
                onDismissed: (_) => _delete(t.id),
                child: Semantics(
                  label: 'task_title: ${t.title}',
                  container: true,
                  child: ListTile(
                    key: ValueKey('task_${t.id}'),
                    leading: Checkbox(value: t.done, onChanged: (_) => _toggleDone(t.id)),
                    title: Text(t.title, style: TextStyle(decoration: t.done ? TextDecoration.lineThrough : null)),
                    subtitle: t.description == null ? null : Text(t.description!),
                    onTap: () => _addOrEdit(task: t),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}
