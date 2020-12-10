import 'package:call_recorder/ui/views/home/home_viewmodel.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:stacked/stacked.dart';

class HomeView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<HomeViewModel>.reactive(
        builder: (context, model, child){
          return Scaffold(
            appBar: AppBar(
              title: Text("Manual Call Recorder"),
            ),
            body: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  RaisedButton(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 16.0),
                      child: Text("Start Recording"),
                    ),
                    onPressed: model.allowRecord?model.startRecording:null,
                  ),
                  SizedBox(height: 24),
                  RaisedButton(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 16.0),
                      child: Text("Stop Recording"),
                    ),
                    onPressed: model.allowStop?model.stopRecording:null,
                  ),
                ],
              ),
            ),
          );
        },
        viewModelBuilder: () => HomeViewModel(),
    );
  }
}
