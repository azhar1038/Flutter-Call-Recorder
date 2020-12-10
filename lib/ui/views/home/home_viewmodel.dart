import 'package:call_recorder/app/locator.dart';
import 'package:call_recorder/services/platform_service.dart';
import 'package:stacked/stacked.dart';

class HomeViewModel extends BaseViewModel{
  PlatformService platform = locator<PlatformService>();
  bool allowRecord = true;
  bool allowStop = false;

  Future<void> startRecording() async {
    allowRecord = false;
    bool hasRecordPermission = await platform.checkRecordPermission();
    bool hasStoragePermission = await platform.requestStoragePermission();
    if(hasRecordPermission && hasStoragePermission){
      allowStop = true;
      try{
        await platform.startRecord();
      }catch(e){
        print(e);
        allowRecord = true;
        allowStop = false;
      }
      notifyListeners();
    }
  }

  Future<void> stopRecording() async {
    allowRecord = true;
    allowStop = false;
    try{
      await platform.stopRecord();
    }catch(e){
      print(e);
    }
    notifyListeners();
  }
}