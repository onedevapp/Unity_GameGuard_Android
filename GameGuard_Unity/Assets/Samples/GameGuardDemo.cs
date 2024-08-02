using TMPro;
using UnityEngine;

namespace OneDevApp.GameGuard.Demo
{
    public class GameGuardDemo : MonoBehaviour
    {
        public TextMeshProUGUI statusTxt;

        // Start is called before the first frame update
        async void Start()
        {

            GameGuardedStatus guardStatus = await new GameGuardBuilder()
                .BlockPackageNameIfNotMatched("")   //Dont use Application.identifier, just Hardcode the package name
                .BlockDualOrCloneSpaceApps(true)
                .BlockRunningInEmulator(true)
                .BlockIfInstalledViaAPK(false)
                .BlockIfRootedDevice(false)
                .BlockIfUsingProxy(false)
                .BlockIfDevOptionsEnabled(false)
                .ToggleLogs(true)
                .GetStatusAsync();  //GetStatusAsync or GetStatus


            /*bool isValid = new GameGuardBuilder()
                .BlockPackageNameIsNotMatched("com.DefaultCompany.MyAwesomeGame")
                .BlockDualOrCloneSpaceApps(true)
                .BlockRunningInEmulator(true)
                .BlockIfInstalledViaAPK(true)
                .BlockIfRootedDevice(true)
                .BlockIfUsingProxy(true)
                .BlockIfDevOptionsEnabled(true)
                .ToggleLogs(true)
                .Validate();    //ValidateAsync or Validate
            */

            statusTxt.SetText("GameGuarded Status: \n" + guardStatus.ToString());
        }
    }
}