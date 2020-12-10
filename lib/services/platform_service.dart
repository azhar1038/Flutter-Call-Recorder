import 'package:flutter/services.dart';
import 'package:injectable/injectable.dart';

@lazySingleton
class PlatformService{
  static const platform = const MethodChannel('com.az.call_recorder');

  Future<bool> requestStoragePermission() async {
    try {
      bool hasPermission = await platform.invokeMethod("requestStoragePermission");
      return hasPermission;
    }catch(e){
      return false;
    }
  }

  Future<bool> checkRecordPermission() async {
    try {
      bool hasPermission = await platform.invokeMethod("checkRecordPermission");
      return hasPermission;
    }catch(e){
      return false;
    }
  }

  Future<void> startRecord() async {
    try {
      await platform.invokeMethod("startRecord");
    }catch(e){
      throw e;
    }
  }

  Future<void> stopRecord() async {
    try {
      await platform.invokeMethod("stopRecord");
    }catch(e){
      throw e;
    }
  }
}