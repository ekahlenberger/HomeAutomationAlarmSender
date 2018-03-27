<p align=center>
  <img alt="Logo" src="fastlane/metadata/android/en-US/images/icon.png" width="100"/>
</p>

# HomeAutomationAlarmSender
Home Automation Alarm sender for android alarm to [openHAB](https://openhab.org) sync

<a href="https://play.google.com/store/apps/details?id=net.kahlenberger.eberhard.haas"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" alt="Get it on Play Store" height="80"></a>

This app let's you sync your next android alarm to your openHAB server. It sends the alarm time as seconds since 01/01/1970 as status update to the specified openHAB item.

- Install the **openHAB REST API** plugin
- Install **HomeAutomationAlarmSender** from any source you like (currently working on getting it into fdroid)
- Create an **openHAB item**
- **Fill in** the REST API URL and item name
- Setup your next alarm in android
- Create rules in openHAB based on the 


## Example
***These example can be done with timers, but I think these are easier to understand.***

**Alarm.items**:
```
Number AndroidAlarm
Switch WakeUpAlarm
``` 

**Alarm.rules**

The "Alarm Trigger" rule checks every 30s if the time is up. If it is it sends the alarm via the WakeUpAlarm Item. 
Let anyone listen on WakeUpAlarm state to react

```
rule "Alarm Trigger"
when
	Time cron "0/30 * 0-12 ? * * *"
then
  val diff = AndroidAlarm.state as Number - now().millis / 1000;
  if (diff <= 0 )
    WakeUpAlarm.sendCommand(ON)        
end
``` 
If you want to create some kind of wake up light scenario you might do something like this:

_This rule checks every 10 seconds between 0 and 10 am if the remaining time to the next alarm is below the offset of 1800 seconds. If it is, the linear interpolated gradient between the WakeUpStartLightColor and MorningLightColor is send to my sleeping room lightbulb._

```

rule "Alarm wakeup light"
when
	Time cron "0/10 * 0-10 ? * * *"
then
  val offset = 1800;
  val diff = WakeUpInput.state as Number - now().millis / 1000;
  if (diff > 0 && diff < offset)
  {
    val factor = (offset-diff) / offset * 0.75;
    val startColor = WakeUpStartLightColor.state as HSBType;
    val targetColor = MorningLightColor.state as HSBType;
    val intermediateColor = new HSBType(new DecimalType((targetColor.hue - startColor.hue) * factor + startColor.hue),                                                    
                                        new PercentType((targetColor.saturation - startColor.saturation) * factor + startColor.saturation),
                                        new PercentType(targetColor.brightness.toBigDecimal * factor))
    SleepingRoomLight.sendCommand(intermediateColor.toString)
  }
end

```
- MorningLightColor is an item: Color MorningLightColor which holds a predefined color I like to wake up to in the morning
- WakeUpStartLightColor is and item: Color WakeUpStartLightColor which holds a very dark color I don't wake up to
- SleepingRommLight is the actual light item connected to my sleeping room lamp


