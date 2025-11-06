import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

import '../models/task.dart';

class TaskService {
  static const _key = 'tasks.v1';
  final SharedPreferences prefs;

  TaskService({required this.prefs});

  Future<void> init() async {
    // Ensure key exists (no-op if already present)
    prefs.getString(_key);
  }

  List<Task> loadTasks() {
    final jsonStr = prefs.getString(_key);
    if (jsonStr == null || jsonStr.isEmpty) return [];
    try {
      final list = jsonDecode(jsonStr) as List<dynamic>;
      return list.map((e) => Task.fromJson(Map<String, dynamic>.from(e))).toList();
    } catch (_) {
      return [];
    }
  }

  Future<void> saveTasks(List<Task> tasks) async {
    final jsonStr = jsonEncode(tasks.map((t) => t.toJson()).toList());
    await prefs.setString(_key, jsonStr);
  }

  Future<List<Task>> addTask(Task task) async {
    final tasks = loadTasks();
    tasks.insert(0, task);
    await saveTasks(tasks);
    return tasks;
  }

  Future<List<Task>> updateTask(Task task) async {
    final tasks = loadTasks();
    final idx = tasks.indexWhere((t) => t.id == task.id);
    if (idx != -1) tasks[idx] = task;
    await saveTasks(tasks);
    return tasks;
  }

  Future<List<Task>> deleteTask(int id) async {
    final tasks = loadTasks();
    tasks.removeWhere((t) => t.id == id);
    await saveTasks(tasks);
    return tasks;
  }

  Future<List<Task>> toggleDone(int id) async {
    final tasks = loadTasks();
    final idx = tasks.indexWhere((t) => t.id == id);
    if (idx != -1) {
      tasks[idx].done = !tasks[idx].done;
      await saveTasks(tasks);
    }
    return tasks;
  }

  /// Remove all tasks
  Future<void> clearAll() async {
    await saveTasks([]);
  }
}
