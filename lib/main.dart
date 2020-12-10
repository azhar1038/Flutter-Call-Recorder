import 'package:call_recorder/app/locator.dart';
import 'package:flutter/material.dart';

import 'app.dart';

void main() async {
  // WidgetsFlutterBinding.ensureInitialized();
  setUpLocator();
  runApp(CallRecorder());
}
