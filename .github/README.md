# Unity Game Guard for Android
This unity plugin provides an extra layer of security to your game from being pirated / cracked / running in clone space or in emulator and more.. 


### Disclaimer
This library protects your game against simple tampering attacks. But, this isn't guaranteed to stop your app from getting pirated. There is no such thing as 100% security, and a determined and skilled attacker with enough time, could remove these checks from the code. The real objective here is to raise the bar out of reach of opportunist and automatic attackers.
<br><br>

### Installation
There are 3 ways to install this plugin:

1. clone/download this repository and move the Plugins folder to your Unity project's Assets folder
2. via Package Manager (**Add package from git url**):

    - `https://github.com/onedevapp/Unity_GameGuard_Android.git`
3. via Package Manager (add the following line to **Packages/manifest.json**):
    - `"com.onedevapp.gameguard": "https://github.com/onedevapp/Unity_GameGuard_Android.git",`

<br>

### How To

Create a new GameGuardBuilder object.

```C#
GameGuardBuilder guardBuiler = new GameGuardBuilder()
```

### Verify your app's signing certificates (signatures)
The app signatures will be broken if the .apk is altered in any way and doesn't match with originaly signed signature.
<br>
```C#
// appSignature : The original APK signature for the PRODUCTION version
// showSignature : If ture then certificate signature will be print to the logcat.
guardBuiler.BlockAppSingatureIsNotMatched(string appSignature, bool showSignature = false)
```

### Verify your app package name
Set the package name to validate whether the current app was running with the exact same package name specified, because some tools can change the package name while rebuilding the APK from the original
<br>

```C#
guardBuiler.BlockPackageNameIsNotMatched(string packageName)
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

### Verify if app is being run in an emulator
Emulators are mostly widley used to hack the games.
<br>
Note: Disabling Emulator might leads to Users loss, since few percentage of users actually enjoys playing the emulators than the actual device. Also this will not detect most of the emulators or the newest one.
<br>

```C#
// block : true to block and default is false
guardBuiler.BlockRunningInEmulator(bool block)
```

### Logs
Toggle logs

```C#
//By default puglin console log will be diabled, but can be enabled
guardBuiler.ToggleLogs(bool showLogs);
```

### Validation

```C#
// Verify the game guard with the config values
bool isValid = guardBuiler.Validate();  //or
bool isValid = await guardBuiler.ValidateAsync();

//Complete Sample
bool isValid = new GameGuardBuilder()
    .BlockPackageNameIsNotMatched("")
    .BlockDualOrCloneSpaceApps(true)
    .BlockRunningInEmulator(true)
    .BlockIfInstalledViaAPK(true)
    .ToggleLogs(false)
    .Validate();    //ValidateAsync or Validate
```

### Get Validation Status

```C#
//or get more details with game guard status
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
    Debugger,
    DevOptions
}

GameGuardedStatus guardStatus = guardBuiler.GetStatus();  //or
GameGuardedStatus guardStatus = await guardBuiler.GetStatusAsync();

//Complete Sample
GameGuardedStatus guardStatus = await new GameGuardBuilder()
    .BlockPackageNameIsNotMatched("")
    .BlockDualOrCloneSpaceApps(true)
    .BlockRunningInEmulator(true)
    .BlockIfInstalledViaAPK(true)
    .ToggleLogs(false)
    .GetStatusAsync();  //GetStatusAsync or GetStatus

```
<br>

## Recommendaton
* [Anti-Cheat Toolkit](https://assetstore.unity.com/packages/tools/utilities/anti-cheat-toolkit-2023-202695) - Protects variables & saves, detects plenty of cheats and many more.
* [Obfuscator](https://assetstore.unity.com/packages/tools/utilities/obfuscator-48919) - This asset obfuscates your code making it harder for bad guys to reverse engineer your projects.
* [Mfuscator: IL2CPP Encryption](https://assetstore.unity.com/packages/tools/utilities/mfuscator-il2cpp-encryption-256631) - Protect Unity IL2CPP builds using etadata encryption and initialization pattern obfuscation and impede the reverse-engineering efforts of hackers targeting your game.

<br>

## Reference
https://proandroiddev.com/preventing-android-app-cloning-e3194269bcfa
https://medium.com/avi-parshan-studios/protecting-your-android-app-against-reverse-engineering-and-tampering-a727768b2e9e

<br>

## :open_hands: Contributions
Any contributions are welcome!

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create New Pull Request

<br><br>