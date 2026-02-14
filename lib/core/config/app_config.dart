import 'dart:html' as html;

class AppConfig {
  static String get apiBaseUrl {
    const envUrl = String.fromEnvironment('API_BASE_URL', defaultValue: '');
    if (envUrl.isNotEmpty) return envUrl;
    return '${html.window.location.origin}/api/v1';
  }

  static const String appNameAr = 'مكتب الصحة والبيئة - أمانة العاصمة';
  static const String appNameEn = 'Health & Environment Office – Capital Secretariat';
  static const Duration connectionTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);
}
