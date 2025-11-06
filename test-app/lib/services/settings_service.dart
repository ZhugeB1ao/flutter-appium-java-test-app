import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsService {
  static const _keyThemeMode = 'settings.themeMode';

  final SharedPreferences prefs;

  SettingsService({required this.prefs});

  Future<void> init() async {
    // placeholder for future migrations
    prefs.getString(_keyThemeMode);
  }

  ThemeMode getThemeMode() {
    final s = prefs.getString(_keyThemeMode);
    switch (s) {
      case 'light':
        return ThemeMode.light;
      case 'dark':
        return ThemeMode.dark;
      default:
        return ThemeMode.system;
    }
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    final s = mode == ThemeMode.system
        ? 'system'
        : (mode == ThemeMode.dark ? 'dark' : 'light');
    await prefs.setString(_keyThemeMode, s);
  }
}
