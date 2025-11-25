import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/semantics.dart';

import 'services/task_service.dart';
import 'pages/tasks_page.dart';

/// Minimal single-screen app exposing basic task CRUD.
Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    SemanticsBinding.instance.ensureSemantics();
  } catch (_) {}

  final prefs = await SharedPreferences.getInstance();
  final taskService = TaskService(prefs: prefs);
  await taskService.init();

  runApp(SimpleTasksApp(taskService: taskService));
}

class SimpleTasksApp extends StatelessWidget {
  final TaskService taskService;
  const SimpleTasksApp({required this.taskService, super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Tasks',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: Scaffold(
        appBar: AppBar(title: const Text('Tasks')),
        body: TasksPage(taskService: taskService),
      ),
    );
  }
}
