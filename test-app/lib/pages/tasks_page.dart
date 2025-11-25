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
  final TextEditingController _addController = TextEditingController();
  final FocusNode _addFocus = FocusNode();
  int? _editingId;
  final Map<int, TextEditingController> _editingControllers = {};
  final Map<int, FocusNode> _editingFocusNodes = {};

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

  // New inline add: single text field + add button
  Future<void> _handleAdd() async {
    final title = _addController.text.trim();
    if (title.isEmpty) return;
    final newTask = Task(id: DateTime.now().millisecondsSinceEpoch, title: title, description: null);
    await widget.taskService.addTask(newTask);
    _addController.clear();
    _addFocus.requestFocus();
    _load();
  }

  // Enter inline edit mode for a task
  void _startEditing(Task task) {
    setState(() {
      _editingId = task.id;
      _editingControllers[task.id] = TextEditingController(text: task.title);
      final f = FocusNode();
      _editingFocusNodes[task.id] = f;
      f.addListener(() {
        if (!f.hasFocus && _editingId == task.id) {
          // focus lost -> commit
          _commitEdit(task.id);
        }
      });
      // request focus after build
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _editingFocusNodes[task.id]!.requestFocus();
      });
    });
  }

  // Commit inline edit (save changes)
  void _commitEdit(int id) async {
    final ctr = _editingControllers[id];
    if (ctr == null) return;
    final newTitle = ctr.text.trim();
    // find task
    final idx = _tasks.indexWhere((t) => t.id == id);
    if (idx != -1) {
      final t = _tasks[idx];
      t.title = newTitle.isEmpty ? t.title : newTitle;
      await widget.taskService.updateTask(t);
      setState(() {
        _editingId = null;
      });
      // cleanup
      _editingControllers[id]?.dispose();
      _editingControllers.remove(id);
      _editingFocusNodes[id]?.dispose();
      _editingFocusNodes.remove(id);
      _load();
    }
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
              // Inline add UI when list is empty
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  SizedBox(
                    width: 220,
                    child: Semantics(
                      label: 'add_task_field',
                      child: TextField(
                        key: const Key('add_task_field'),
                        controller: _addController,
                        focusNode: _addFocus,
                        decoration: const InputDecoration(hintText: 'Nhập tiêu đề...'),
                        onSubmitted: (_) => _handleAdd(),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Semantics(
                    label: 'add_task_button',
                    button: true,
                    child: ElevatedButton(
                      key: const Key('add_task_button'),
                      onPressed: _handleAdd,
                      child: const Text('Thêm'),
                    ),
                  ),
                ],
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
          child: Row(
            children: [
              Expanded(
                child: Semantics(
                  label: 'add_task_field',
                  child: TextField(
                    key: const Key('add_task_field'),
                    controller: _addController,
                    focusNode: _addFocus,
                    decoration: const InputDecoration(hintText: 'Nhập tiêu đề...'),
                    onSubmitted: (_) => _handleAdd(),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Semantics(
                label: 'add_task_button',
                button: true,
                child: ElevatedButton(
                  key: const Key('add_task_button'),
                  onPressed: _handleAdd,
                  child: const Text('Thêm'),
                ),
              ),
            ],
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
                    title: _editingId == t.id
                        ? TextField(
                            key: ValueKey('task_edit_field_${t.id}'),
                            controller: _editingControllers[t.id],
                            focusNode: _editingFocusNodes[t.id],
                            onSubmitted: (_) => _commitEdit(t.id),
                            decoration: const InputDecoration(border: InputBorder.none),
                          )
                        : GestureDetector(
                            onDoubleTap: () => _startEditing(t),
                            child: Text(t.title, style: TextStyle(decoration: t.done ? TextDecoration.lineThrough : null)),
                          ),
                    subtitle: t.description == null ? null : Text(t.description!),
                    trailing: Semantics(
                      label: 'delete_task_button',
                      hint: 'Xoá công việc',
                      child: IconButton(
                        key: ValueKey('delete_task_button_${t.id}'),
                        icon: const Icon(Icons.close),
                        onPressed: () => _delete(t.id),
                        tooltip: 'Xoá',
                      ),
                    ),
                    onTap: null,
                    onLongPress: () => _toggleDone(t.id),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  @override
  void dispose() {
    _addController.dispose();
    _addFocus.dispose();
    for (final c in _editingControllers.values) {
      c.dispose();
    }
    for (final f in _editingFocusNodes.values) {
      f.dispose();
    }
    super.dispose();
  }
}
