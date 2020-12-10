import 'package:call_recorder/app/app_theme.dart';
import 'package:call_recorder/ui/views/home/home_view.dart';
import 'package:flutter/material.dart';

class CallRecorder extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "Call Recorder",
      theme: appTheme,
      home: HomeView(),
    );
  }
}
