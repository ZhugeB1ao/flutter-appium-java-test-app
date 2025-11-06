import 'package:flutter/material.dart';

import '../services/settings_service.dart';
import '../services/task_service.dart';

class SettingsPage extends StatefulWidget {
  final ThemeMode currentMode;
  final ValueChanged<ThemeMode> onThemeChanged;
  final SettingsService settingsService;
  final TaskService taskService;
  const SettingsPage({required this.currentMode, required this.onThemeChanged, required this.settingsService, required this.taskService, super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  late ThemeMode _mode;

  @override
  void initState() {
    super.initState();
    _mode = widget.currentMode;
  }

  Future<void> _setMode(ThemeMode m) async {
    setState(() => _mode = m);
    widget.onThemeChanged(m);
    await widget.settingsService.setThemeMode(m);
  }

  Future<void> _clearTasks() async {
    // Show confirmation dialog before clearing all tasks
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Xác nhận'),
        content: const Text('Bạn có chắc muốn xoá tất cả công việc?'),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Huỷ')),
          ElevatedButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Xác nhận')),
        ],
      ),
    );

    if (confirm == true) {
      await widget.taskService.clearAll();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Đã xoá tất cả công việc')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Cài đặt', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          const Text('Giao diện', style: TextStyle(fontWeight: FontWeight.w600)),
          RadioListTile<ThemeMode>(
            title: const Text('Hệ thống (System)'),
            value: ThemeMode.system,
            groupValue: _mode,
            onChanged: (v) => _setMode(v ?? ThemeMode.system),
          ),
          RadioListTile<ThemeMode>(
            title: const Text('Sáng (Light)'),
            value: ThemeMode.light,
            groupValue: _mode,
            onChanged: (v) => _setMode(v ?? ThemeMode.light),
          ),
          RadioListTile<ThemeMode>(
            title: const Text('Tối (Dark)'),
            value: ThemeMode.dark,
            groupValue: _mode,
            onChanged: (v) => _setMode(v ?? ThemeMode.dark),
          ),
          const SizedBox(height: 12),
          Semantics(
            label: 'clear_all_tasks_button',
            child: ElevatedButton.icon(
              onPressed: _clearTasks,
              icon: const Icon(Icons.delete_forever),
              label: const Text('Xoá tất cả công việc'),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            ),
          ),
          const SizedBox(height: 12),
          const Text('App mẫu để thử nghiệm. Lựa chọn theme sẽ được lưu.')
        ],
      ),
    );
  }
}
