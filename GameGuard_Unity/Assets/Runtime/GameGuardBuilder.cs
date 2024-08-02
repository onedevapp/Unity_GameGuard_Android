using System;
using System.Threading.Tasks;

#if UNITY_ANDROID && !UNITY_EDITOR
using UnityEngine;
#endif

namespace OneDevApp.GameGuard
{

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


    public class GameGuardBuilder
    {
        private GameGuardIntenral _gameGuard = new GameGuardIntenral();

        /// <summary>
        /// Set the app signature to validate whether the current app signature was modified.
        /// Note: Don't use this method when using Google Play App Signing since Google removes the original signature and add another one, so this method will fail.
        /// </summary>
        /// <param name="appSingature">App Signature to be verified</param>
        /// <param name="showSignature">true to get your current certificate signature to the Logcat and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder BlockAppSingatureIsNotMatched(string appSignature, bool showSignature = false)
        {
            _gameGuard.myAppSignature = appSignature;
            _gameGuard.showSignature = showSignature;
            return this;
        }

        /// <summary>
        /// Set the package name to validate whether the current app was running with the exact same package name specified, because some tools can change the package name while rebuilding the APK from the original 
        /// </summary>
        /// <param name="packageName">Package Name to be verified</param>
        /// <returns></returns>
        public GameGuardBuilder BlockPackageNameIfNotMatched(string packageName)
        {
            _gameGuard.myPackageName = packageName;
            return this;
        }

        /// <summary>
        /// Check whether the source of installation is APK or not, by doing so, we are allowing only installing APP via any Store such as PlayStore, Amazon Store etc.
        /// </summary>
        /// <param name="block">true to block and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder BlockIfInstalledViaAPK(bool block)
        {
            _gameGuard.noPackageInstaller = block;
            return this;
        }

        /// <summary>
        /// Some tools can run the build in Clone or Dual Space to hack the game 
        /// </summary>
        /// <param name="block">true to block and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder BlockDualOrCloneSpaceApps(bool block)
        {
            _gameGuard.noDualOrCloneSpace = block;
            return this;
        }

        /// <summary>
        /// Emulators are mostly widley used to hack the games in which root user can accessed easliy.
        /// Note: Disabling Emulator might leads to Users loss, since few percentage of users actually enjoys playing the emulators than the actual device. Also this will not detect most of the emulators or the newest one.
        /// </summary>
        /// <param name="block">true to block and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder BlockRunningInEmulator(bool block)
        {
            _gameGuard.noEmulator = block;
            return this;
        }

        /// <summary>
        /// Checks if the device is rooted.
        /// </summary>
        /// <param name="block">true to block and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder BlockIfRootedDevice(bool block)
        {
            _gameGuard.noRoot = block;
            return this;
        }

        /// <summary>
        /// Checks if the device using proxy.
        /// </summary>
        /// <param name="block">true to block and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder BlockIfUsingProxy(bool block)
        {
            _gameGuard.noProxy = block;
            return this;
        }

        /// <summary>
        /// Checks the device whether Developer Options is enabled or not.
        /// </summary>
        /// <param name="block">true to block and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder BlockIfDevOptionsEnabled(bool block)
        {
            _gameGuard.noDevOptions = block;
            return this;
        }

        /// <summary>
        /// Check whether the source of installation is APK or not, by doing so, we are allowing only installing APP via any Store such as PlayStore, Amazon Store etc.
        /// </summary>
        /// <param name="showLogs">true to show Logs and default is false</param>
        /// <returns></returns>
        public GameGuardBuilder ToggleLogs(bool showLogs)
        {
            _gameGuard.enableLogs = showLogs;
            return this;
        }

        /// <summary>
        /// Verify the game guard with the config values
        /// </summary>
        /// <returns>true is any conditions not met else false and other than supported platform (Android) always return true</returns>
        public bool Validate()
        {
            int status = 0;
#if UNITY_ANDROID && !UNITY_EDITOR
            status = _gameGuard.validateGame();
#endif
            _gameGuard.Dispose();
            return status == 0;
        }

        /// <summary>
        /// Verify the game guard with the config values and return the GameGuardedStatus value
        /// </summary>
        /// <returns>GameGuardedStatus value</returns>
        public GameGuardedStatus GetStatus()
        {
            int status = 0;
#if UNITY_ANDROID && !UNITY_EDITOR
            status = _gameGuard.validateGame();
#endif
            _gameGuard.Dispose();
            return (GameGuardedStatus)status;
        }

#pragma warning disable 1998
        /// <summary>
        /// Verify the game guard with the config values
        /// </summary>
        /// <returns>true is any conditions not met else false and other than supported platform (Android) always return true</returns>
        public async Task<bool> ValidateAsync()
        {
            int status = 0;
#if UNITY_ANDROID && !UNITY_EDITOR
            status = await _gameGuard.validateGameAysnc();
#endif
            _gameGuard.Dispose();
            return status == 0;
        }

        /// <summary>
        /// Verify the game guard with the config values and return the GameGuardedStatus value
        /// </summary>
        /// <returns>GameGuardedStatus value</returns>
        public async Task<GameGuardedStatus> GetStatusAsync()
        {
            int status = 0;
#if UNITY_ANDROID && !UNITY_EDITOR
            status = await _gameGuard.validateGameAysnc();
#endif
            _gameGuard.Dispose();
            return (GameGuardedStatus) status;
        }

#pragma warning restore 1998
    }

    internal class GameGuardIntenral : IDisposable
    {
#if UNITY_ANDROID && !UNITY_EDITOR

        private AndroidJavaObject cls_jni;
#endif
        internal string myAppSignature { get; set; }
        internal string myPackageName { get; set; }
        internal bool noDualOrCloneSpace { get; set; }
        internal bool noEmulator { get; set; }
        internal bool noPackageInstaller { get; set; }
        internal bool noRoot { get; set; }
        internal bool noProxy { get; set; }
        internal bool noDevOptions { get; set; }
        internal bool enableLogs { get; set; }
        internal bool showSignature { get; set; }


#if UNITY_ANDROID && !UNITY_EDITOR
        internal int validateGame()
        {
            cls_jni = new AndroidJavaObject("com.onedevapp.appguard.VerifyAppGuard");

            using (AndroidJavaClass cls_UnityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
            {

                using (AndroidJavaObject obj_Activity = cls_UnityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                {

                    return cls_jni.Call<int>("guardAppWith", obj_Activity, myAppSignature, myPackageName, noDualOrCloneSpace, noEmulator, noPackageInstaller, noRoot, noProxy, noDevOptions, noDevOptions, enableLogs, showSignature);

                }
            }
        }

        internal async Task<int> validateGameAysnc()
        {
            return await Task.Run<int>(() =>
            {
                AndroidJNI.AttachCurrentThread();
                cls_jni = new AndroidJavaObject("com.onedevapp.appguard.VerifyAppGuard");

                using (AndroidJavaClass cls_UnityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                {

                    using (AndroidJavaObject obj_Activity = cls_UnityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                    {
                        int toReturn = cls_jni.Call<int>("guardAppWith", obj_Activity, myAppSignature, myPackageName, noDualOrCloneSpace, noEmulator, noPackageInstaller, noRoot, noProxy, noDevOptions, noDevOptions, enableLogs, showSignature);
                        AndroidJNI.DetachCurrentThread();
                        return toReturn;
                    }
                }
            });

        }
#endif

        public void Dispose()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            cls_jni.Dispose();
            cls_jni = null;
#endif
        }

        ~GameGuardIntenral()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            if (cls_jni != null)
                cls_jni.Dispose();
#endif
        }
    }

}
