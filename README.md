# Unity Game Guard for Android
This unity plugin provides an extra layer of security to your game from being pirated / cracked / running in clone space or in emulator and more.. 


### Disclaimer
This library protects your game against simple tampering attacks. But, this isn't guaranteed to stop your app from getting pirated. There is no such thing as 100% security, and a determined and skilled attacker with enough time, could remove these checks from the code. The real objective here is to raise the bar out of reach of opportunist and automatic attackers.
<br><br>

### Installation

* Open Package Manager in Unity
* Press "+" and "Add package from git URL..."
* Enter this repository and version as  `https://github.com/onedevapp/Unity_GameGuard_Android.git#TAG` (see latest version [here](https://github.com/onedevapp/Unity_GameGuard_Android/tags))

<br>

### How To

Create a new GameGuardBuilder object.

```C#
GameGuardBuilder guardBuiler = new GameGuardBuilder()
```
<br>

### Verify your app's signing certificates (signatures)
The app signatures will be broken if the .apk is altered in any way and doesn't match with originaly signed signature.

> [!Warning]
> Don't use this method when using Google Play App Signing since Google removes the original signature and add another one, so this method will fail.

<br>

```C#
// appSignature : The original APK signature for the PRODUCTION version, empty value will not validate signature
// showSignature : If ture then certificate signature will be print to the logcat.
guardBuiler.BlockAppSingatureIsNotMatched(string appSignature, bool showSignature = false)
```

> [!Note]
> To get the App signature, pass empty string with showSignature as true, once you had your signature, set appSignature with your signature and showSignature as false to validate


### Verify your app package name
Set the package name to validate whether the current app was running with the exact same package name specified, because some tools can change the package name while rebuilding the APK from the original
<br>

```C#
guardBuiler.BlockPackageNameIsNotMatched(string packageName)    // Dont use Application.identifier, just Hardcode/pass the value from server
```

### Verify the installation source
Check whether the source of installation is APK or not, by doing so, we are allowing only installing APP via any Store such as PlayStore, Amazon Store etc.
<br>

```C#
// block : true to block and default is false
guardBuiler.BlockIfInstalledViaAPK(bool block)
```

### Verify if app is being run in clone space or dual space
Some tools can run the build in Clone or Dual Space to hack the game 
<br>

```C#
// block : true to block and default is false
guardBuiler.BlockDualOrCloneSpaceApps(bool block)
```

### Verify if app is running in an emulator
Emulators are most widley used to hack the games.
<br>

Note: 
* Disabling Emulator might leads to users loss, since few percentage of users actually enjoys playing in the emulators than the actual device.
* Uses [Android-Emulator-Detection](https://github.com/reveny/Android-Emulator-Detection.git) library by [reveny](https://github.com/reveny) to detect emulator 
<br>

```C#
// block : true to block and default is false
guardBuiler.BlockRunningInEmulator(bool block)
```

### Verify if device is rooted
Checks if the device is rooted. 
<br>

```C#
// block : true to block and default is false
guardBuiler.BlockIfRootedDevice(bool block)
```

### Verify if device usign proxy
Checks if the device using proxy.
<br>

```C#
// block : true to block and default is false
guardBuiler.BlockIfUsingProxy(bool block)
```

### Verify if developer options enabled
Checks the device whether Developer Options is enabled or not. Note that by enabling this, it also checks for whether Debugger is attched
<br>

```C#
// block : true to block and default is false
guardBuiler.BlockIfDevOptionsEnabled(bool block)
```


### Logs
Toggle logs

```C#
// By default puglin console log will be diabled, but can be enabled
guardBuiler.ToggleLogs(bool showLogs);
```

### Validation

```C#
// Verify the game guard with the config values
bool isValid = guardBuiler.Validate();  // or
bool isValid = await guardBuiler.ValidateAsync();

// Complete Sample
bool isValid = new GameGuardBuilder()
    .BlockPackageNameIsNotMatched("com.DefaultCompany.MyAwesomeGame")   // Dont use Application.identifier, just Hardcode the package name
    .BlockDualOrCloneSpaceApps(true)
    .BlockRunningInEmulator(true)
    .BlockIfInstalledViaAPK(true)
    .BlockIfRootedDevice(true)
    .BlockIfUsingProxy(true)
    .BlockIfDevOptionsEnabled(true)
    .ToggleLogs(true)
    .Validate();    // ValidateAsync or Validate
```

### Get Validation Status

```C#
// or get more details with game guard status
public enum GameGuardedStatus
{
    Valid = 0,
    AppSingature,
    PackageNameChanged,
    UnknownPackageInstaller,
    InstalledViaAPK,
    InDualSpace,
    InClonedSpace,
    ZygoteCountTwice,
    IsEmulator,
    IsRooted,
    UsingProxy,
    Debugger,
    DevOptions
}

GameGuardedStatus guardStatus = guardBuiler.GetStatus();  //or
GameGuardedStatus guardStatus = await guardBuiler.GetStatusAsync();

// Complete Sample
GameGuardedStatus guardStatus = await new GameGuardBuilder()
    .BlockPackageNameIfNotMatched("com.DefaultCompany.MyAwesomeGame")   // Dont use Application.identifier, just Hardcode the package name
    .BlockDualOrCloneSpaceApps(true)
    .BlockRunningInEmulator(true)
    .BlockIfInstalledViaAPK(true)
    .BlockIfRootedDevice(true)
    .BlockIfUsingProxy(true)
    .BlockIfDevOptionsEnabled(true)
    .ToggleLogs(true)
    .GetStatusAsync();  // GetStatusAsync or GetStatus

```
<br>

## Recommendaton
* [ByteProtector](https://assetstore.unity.com/packages/tools/utilities/byteprotector-mobile-anti-cheat-286004) - ByteProtector is a lightweight anti-cheat for Android™ games. It helps game developers detect cheating methods like code injections, debuggers, emulators, root, and Xposed.
* [Anti-Cheat Toolkit](https://assetstore.unity.com/packages/tools/utilities/anti-cheat-toolkit-2023-202695) - Protects variables & saves, detects plenty of cheats and many more.
* [Obfuscator](https://assetstore.unity.com/packages/tools/utilities/obfuscator-48919) - This asset obfuscates your code making it harder for bad guys to reverse engineer your projects.
* [Mfuscator: IL2CPP Encryption](https://assetstore.unity.com/packages/tools/utilities/mfuscator-il2cpp-encryption-256631) - Protect Unity IL2CPP builds using etadata encryption and initialization pattern obfuscation and impede the reverse-engineering efforts of hackers targeting your game.

<br>

## :open_hands: Contributions
Any contributions are welcome!

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create New Pull Request

<br><br>
