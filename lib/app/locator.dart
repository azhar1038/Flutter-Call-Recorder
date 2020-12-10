import 'package:get_it/get_it.dart';
import 'package:injectable/injectable.dart';

import 'locator.config.dart';

final locator = GetIt.instance;

@injectableInit
Future setUpLocator() async {
  $initGetIt(locator);
  await locator.allReady();
}