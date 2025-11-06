import 'package:flutter/material.dart';

import '../models/task.dart';
import '../services/task_service.dart';
import 'tasks_page.dart';

class HomePage extends StatefulWidget {
  final TaskService taskService;
  const HomePage({required this.taskService, super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
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
              TextFormField(
                controller: titleController,
                decoration: const InputDecoration(labelText: 'Tiêu đề'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Vui lòng nhập tiêu đề' : null,
              ),
              TextFormField(
                controller: descController,
                decoration: const InputDecoration(labelText: 'Mô tả (tuỳ chọn)'),
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

  Widget _buildStatCard(String label, int value, Color color) {
    return Expanded(
      child: Card(
        color: color,
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(label, style: const TextStyle(color: Colors.white70)),
              const SizedBox(height: 8),
              Text('$value', style: const TextStyle(fontSize: 22, color: Colors.white, fontWeight: FontWeight.bold)),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());

    final total = _tasks.length;
    final done = _tasks.where((t) => t.done).length;
    final pending = total - done;
    final recent = _tasks.take(3).toList();

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Simple counter for E2E testing: exposed via Semantics
          Row(
            children: [
              Semantics(
                label: 'counter',
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(borderRadius: BorderRadius.circular(8), color: Colors.blue.shade50),
                  child: Text('Counter: 0', key: const Key('counter_text')),
                ),
              ),
              const SizedBox(width: 12),
              Semantics(
                label: 'increment',
                child: IconButton(
                  key: const Key('increment_button'),
                  onPressed: () {},
                  icon: const Icon(Icons.add),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          const Text('Tổng quan', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 12),
          Row(
            children: [
              _buildStatCard('Tổng', total, Theme.of(context).colorScheme.primary),
              const SizedBox(width: 8),
              _buildStatCard('Hoàn thành', done, Colors.green),
              const SizedBox(width: 8),
              _buildStatCard('Chưa xong', pending, Colors.orange),
            ],
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Công việc gần đây', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
              Semantics(
                label: 'add_task_button',
                child: TextButton.icon(
                  key: const Key('add_task_button'),
                  onPressed: () => _addOrEdit(),
                  icon: const Icon(Icons.add),
                  label: const Text('Thêm'),
                ),
              )
            ],
          ),
          if (recent.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 12.0),
              child: Text('Không có công việc nào gần đây.'),
            )
          else
            ...recent.map((t) => Card(
                  child: ListTile(
                    leading: Checkbox(value: t.done, onChanged: (_) async { await widget.taskService.toggleDone(t.id); _load(); }),
                    title: Text(t.title, style: TextStyle(decoration: t.done ? TextDecoration.lineThrough : null)),
                    subtitle: t.description == null ? null : Text(t.description!),
                    trailing: PopupMenuButton<String>(
                      onSelected: (v) async {
                        if (v == 'edit') {
                          await _addOrEdit(task: t);
                        } else if (v == 'delete') {
                          await widget.taskService.deleteTask(t.id);
                          _load();
                        }
                      },
                      itemBuilder: (context) => [
                        const PopupMenuItem(value: 'edit', child: Text('Chỉnh sửa')),
                        const PopupMenuItem(value: 'delete', child: Text('Xoá')),
                      ],
                    ),
                  ),
                )),
          const SizedBox(height: 12),
          Semantics(
            label: 'view_all_tasks_button',
            child: ElevatedButton.icon(
              onPressed: () {
                // Navigate to Tasks page by switching bottom nav index.
                // We don't have direct access to parent index; just open Tasks full-screen.
                Navigator.of(context).push(MaterialPageRoute(builder: (_) => TasksFullScreen(taskService: widget.taskService)));
              },
              icon: const Icon(Icons.list),
              label: const Text('Xem tất cả công việc'),
            ),
          ),
        ],
      ),
    );
  }
}

class TasksFullScreen extends StatelessWidget {
  final TaskService taskService;
  const TasksFullScreen({required this.taskService, super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Tất cả công việc')),
      body: TasksPage(taskService: taskService),
    );
  }
}
