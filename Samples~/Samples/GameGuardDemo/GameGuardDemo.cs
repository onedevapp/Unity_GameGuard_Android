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
                .BlockPackageNameIsNotMatched("")
                .BlockDualOrCloneSpaceApps(true)
                .BlockRunningInEmulator(true)
                .BlockIfInstalledViaAPK(true)
                .ToggleLogs(true)
                .GetStatusAsync();  //GetStatusAsync or GetStatus


            /*bool isValid = new GameGuardBuilder()
                .BlockPackageNameIsNotMatched("")
                .BlockDualOrCloneSpaceApps(true)
                .BlockRunningInEmulator(true)
                .BlockIfInstalledViaAPK(true)
                .ToggleLogs(true)
                .Validate();    //ValidateAsync or Validate
            */
            
            statusTxt.SetText("GameGuarded Status: \n" + guardStatus.ToString());
        }
    }
}