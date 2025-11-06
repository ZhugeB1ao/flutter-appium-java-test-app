import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/semantics.dart';

import 'services/task_service.dart';
import 'services/settings_service.dart';
import 'pages/tasks_page.dart';
import 'pages/settings_page.dart';
import 'pages/home_page.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // Force-enable Flutter semantics so Appium (UiAutomator2)
  // can discover and interact with widgets via accessibility tree.
  // This is a no-op if semantics are already enabled by the OS.
  try { SemanticsBinding.instance.ensureSemantics(); } catch (_) {}
  final prefs = await SharedPreferences.getInstance();
  final taskService = TaskService(prefs: prefs);
  await taskService.init();

  final settingsService = SettingsService(prefs: prefs);
  await settingsService.init();

  runApp(MyApp(taskService: taskService, settingsService: settingsService));
}

class MyApp extends StatefulWidget {
  final TaskService taskService;
  final SettingsService settingsService;
  const MyApp({required this.taskService, required this.settingsService, super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  ThemeMode _themeMode = ThemeMode.system;
  int _selectedIndex = 0;

  void _onThemeChanged(ThemeMode mode) {
    setState(() {
      _themeMode = mode;
    });
    widget.settingsService.setThemeMode(mode);
  }

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
      // initialize theme from settings on first build
      if (_themeMode == ThemeMode.system) {
        _themeMode = widget.settingsService.getThemeMode();
      }

    final pages = <Widget>[
      TasksPage(taskService: widget.taskService),
      HomePage(taskService: widget.taskService),
      SettingsPage(
        currentMode: _themeMode,
        onThemeChanged: _onThemeChanged,
        settingsService: widget.settingsService,
        taskService: widget.taskService,
      ),
    ];

    return MaterialApp(
      title: 'Test App - Enhanced',
      theme: ThemeData(primarySwatch: Colors.blue, useMaterial3: true),
      darkTheme: ThemeData.dark(),
      themeMode: _themeMode,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Test App'),
        ),
        body: pages[_selectedIndex],
        bottomNavigationBar: BottomNavigationBar(
          currentIndex: _selectedIndex,
          onTap: _onItemTapped,
          items: const [
            BottomNavigationBarItem(icon: Icon(Icons.checklist), label: 'Tasks'),
            BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
            BottomNavigationBarItem(icon: Icon(Icons.settings), label: 'Settings'),
          ],
        ),
      ),
    );
  }
}
