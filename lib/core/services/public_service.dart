import '../models/public_content_model.dart';
import 'api_service.dart';

class PublicService {
  final ApiService _api;

  PublicService(this._api);

  Future<List<PublicContent>> getContent(String category) async {
    final response = await _api.get('/public/content/$category');
    return (response as List).map((e) => PublicContent.fromJson(e)).toList();
  }

  Future<void> submitComplaint(Map<String, dynamic> data) async {
    await _api.post('/public/complaints', body: data);
  }

  Future<Map<String, dynamic>> getAbout() async {
    return await _api.get('/public/about');
  }

  Future<Map<String, dynamic>> getContact() async {
    return await _api.get('/public/contact');
  }
}
