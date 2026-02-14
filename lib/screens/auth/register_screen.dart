import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/providers/auth_provider.dart';
import '../../core/theme/app_theme.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _firstNameCtrl = TextEditingController();
  final _middleNameCtrl = TextEditingController();
  final _lastNameCtrl = TextEditingController();
  final _nationalIdCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmPasswordCtrl = TextEditingController();

  bool _isLoading = false;
  String? _error;

  @override
  void dispose() {
    _firstNameCtrl.dispose();
    _middleNameCtrl.dispose();
    _lastNameCtrl.dispose();
    _nationalIdCtrl.dispose();
    _phoneCtrl.dispose();
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    _confirmPasswordCtrl.dispose();
    super.dispose();
  }

  Future<void> _register() async {
    if (_firstNameCtrl.text.trim().isEmpty ||
        _lastNameCtrl.text.trim().isEmpty ||
        _nationalIdCtrl.text.trim().isEmpty ||
        _phoneCtrl.text.trim().isEmpty ||
        _passwordCtrl.text.isEmpty) {
      setState(() => _error = 'يرجى ملء جميع الحقول الإجبارية');
      return;
    }

    if (_passwordCtrl.text != _confirmPasswordCtrl.text) {
      setState(() => _error = 'كلمات المرور غير متطابقة');
      return;
    }

    setState(() {
      _isLoading = true;
      _error = null;
    });

    final auth = context.read<AuthProvider>();
    try {
      final success = await auth.registerOwner(
        firstName: _firstNameCtrl.text.trim(),
        middleName: _middleNameCtrl.text.trim(),
        lastName: _lastNameCtrl.text.trim(),
        nationalId: _nationalIdCtrl.text.trim(),
        phoneNumber: _phoneCtrl.text.trim(),
        email: _emailCtrl.text.trim(),
        password: _passwordCtrl.text,
        confirmPassword: _confirmPasswordCtrl.text,
      );

      if (mounted && success) {
        setState(() => _isLoading = false);
        showDialog(
          context: context,
          barrierDismissible: false,
          builder: (context) => AlertDialog(
            title: const Text('تم التسجيل بنجاح', textAlign: TextAlign.right),
            content: const Text('تم إنشاء حسابك بنجاح. يمكنك الآن تسجيل الدخول مباشرة.', textAlign: TextAlign.right),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.pop(context); // Close dialog
                  context.push('/login');
                },
                child: const Text('تسجيل الدخول'),
              ),
            ],
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
          _error = 'فشل تسجيل الحساب: $e';
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final loc = AppLocalizations.of(context)!;
    final size = MediaQuery.of(context).size;
    final isWide = size.width > 900;

    return Scaffold(
      body: Row(
        children: [
          if (isWide)
            Expanded(
              flex: 4,
              child: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [AppTheme.primaryDark, AppTheme.primaryGreen],
                  ),
                ),
                child: Center(
                  child: Padding(
                    padding: const EdgeInsets.all(48.0),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          width: 120, height: 120,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: Colors.white.withOpacity(0.1),
                            border: Border.all(color: AppTheme.accentGold, width: 2),
                          ),
                          child: const Icon(Icons.person_add_outlined, color: Colors.white, size: 60),
                        ).animate().scale(duration: 600.ms, curve: Curves.easeOutBack),
                        const SizedBox(height: 32),
                        const Text(
                          'مرحباً بك في بوابتنا الرقمية',
                          style: TextStyle(color: Colors.white, fontSize: 32, fontWeight: FontWeight.bold),
                          textAlign: TextAlign.center,
                        ).animate().fadeIn(delay: 200.ms).slideY(begin: 0.2),
                        const SizedBox(height: 16),
                        Text(
                          'خطوات بسيطة تفصلك عن الوصول لجميع الخدمات الصحية الإلكترونية بكل سهولة وأمان',
                          style: TextStyle(color: Colors.white.withOpacity(0.8), fontSize: 18),
                          textAlign: TextAlign.center,
                        ).animate().fadeIn(delay: 400.ms).slideY(begin: 0.2),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          Expanded(
            flex: 6,
            child: Container(
              color: Colors.grey.shade50,
              child: Center(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(40),
                  child: ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 600),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        if (!isWide) ...[
                          const Icon(Icons.local_hospital, size: 48, color: AppTheme.primaryGreen),
                          const SizedBox(height: 16),
                        ],
                        Text(
                          loc.translate('registerAccount') ?? 'إنشاء حساب جديد',
                          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: AppTheme.primaryDark,
                          ),
                          textAlign: isWide ? TextAlign.right : TextAlign.center,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'يرجى إكمال البيانات التالية بدقة',
                          style: TextStyle(color: Colors.grey.shade600, fontSize: 16),
                          textAlign: isWide ? TextAlign.right : TextAlign.center,
                        ),
                        const SizedBox(height: 32),
                        
                        // Personal Info Section
                        _buildSectionHeader(Icons.person_outline, 'البيانات الشخصية'),
                        const SizedBox(height: 16),
                        Row(
                          children: [
                            Expanded(child: _buildField('اللقب', _lastNameCtrl, Icons.family_restroom)),
                            const SizedBox(width: 12),
                            Expanded(child: _buildField('الاسم الأوسط', _middleNameCtrl, Icons.person_outline)),
                            const SizedBox(width: 12),
                            Expanded(child: _buildField('الاسم الأول', _firstNameCtrl, Icons.person)),
                          ],
                        ),
                        
                        const SizedBox(height: 24),
                        _buildSectionHeader(Icons.contact_emergency_outlined, 'بيانات التواصل والتحقق'),
                        const SizedBox(height: 16),
                        _buildField('رقم الهاتف المحمول', _phoneCtrl, Icons.phone, keyboardType: TextInputType.phone),
                        const SizedBox(height: 12),
                        _buildField('رقم الهوية الوطنية', _nationalIdCtrl, Icons.badge_outlined),
                        const SizedBox(height: 12),
                        _buildField('البريد الإلكتروني', _emailCtrl, Icons.email_outlined, keyboardType: TextInputType.emailAddress),
                        
                        const SizedBox(height: 24),
                        _buildSectionHeader(Icons.lock_outline, 'الأمان'),
                        const SizedBox(height: 16),
                        _buildField('كلمة المرور الجديدة', _passwordCtrl, Icons.lock, obscureText: true),
                        const SizedBox(height: 12),
                        _buildField('تأكيد كلمة المرور', _confirmPasswordCtrl, Icons.lock_clock_outlined, obscureText: true),
                        
                        const SizedBox(height: 32),
                        if (_error != null)
                          Container(
                            padding: const EdgeInsets.all(12),
                            margin: const EdgeInsets.only(bottom: 24),
                            decoration: BoxDecoration(
                              color: AppTheme.errorRed.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(8),
                              border: Border.all(color: AppTheme.errorRed.withOpacity(0.3)),
                            ),
                            child: Row(
                              children: [
                                const Icon(Icons.error_outline, color: AppTheme.errorRed),
                                const SizedBox(width: 12),
                                Expanded(child: Text(_error!, style: const TextStyle(color: AppTheme.errorRed, fontWeight: FontWeight.bold))),
                              ],
                            ),
                          ).animate().shake(),

                        ElevatedButton(
                          onPressed: _isLoading ? null : _register,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppTheme.primaryGreen,
                            padding: const EdgeInsets.symmetric(vertical: 20),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            elevation: 4,
                          ),
                          child: _isLoading
                              ? const SizedBox(width: 24, height: 24, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 3))
                              : const Text('إنشاء الحساب الآن', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white)),
                        ),
                        const SizedBox(height: 24),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            TextButton(
                              onPressed: () => context.push('/login'),
                              child: const Text('تسجيل دخول', style: TextStyle(color: AppTheme.primaryGreen, fontWeight: FontWeight.bold, fontSize: 16)),
                            ),
                            const Text('لديك حساب بالفعل؟', style: TextStyle(fontSize: 16)),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(IconData icon, String title) {
    return Row(
      children: [
        const Expanded(child: Divider()),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              Text(title, style: const TextStyle(fontWeight: FontWeight.bold, color: AppTheme.primaryDark)),
              const SizedBox(width: 8),
              Icon(icon, size: 18, color: AppTheme.primaryGreen),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildField(String label, TextEditingController controller, IconData icon, {bool obscureText = false, TextInputType? keyboardType}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        TextField(
          controller: controller,
          obscureText: obscureText,
          keyboardType: keyboardType,
          textAlign: TextAlign.right,
          decoration: InputDecoration(
            hintText: label,
            prefixIcon: Icon(icon, color: AppTheme.primaryGreen.withOpacity(0.7)),
            filled: true,
            fillColor: Colors.white,
            contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.grey.shade300),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.grey.shade300),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(color: AppTheme.primaryGreen, width: 2),
            ),
          ),
        ),
      ],
    );
  }
}
