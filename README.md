# sc-PlayerButton
Create a simple button than play a media source.
<br />
The basic function are simple a Play/Stop on pressing the button.
The button will show the wave-form on playing the media.

> **IMPORTANT**<br />
> For showing the wave form require the RECORD_AUDIO permission.<br />
> If not, will show a PLAY/STOP simple icon and NOT will show the wave form.
<br />

![image](https://github.com/Paroca72/sc-player/blob/master/raw/1.jpg)
![image](https://github.com/Paroca72/sc-player/blob/master/raw/2.jpg)

This class inherit from the android View class.<br/ >
So for example if you would change the background is enough to use the `android:background` property.
For customize the component please read guide below.

#### Public methods

- **long getDuration()**<br />
Get back the media duration.

- **boolean isPlaying()**<br />
Get the media playing status.

- **void play()**<br />
Start to play the current media

- **void stop()**<br />
Stop to play the current media

- **void setOnEventListener(OnEventListener listener)**<br />
Set the event listener.

<br />

#### Getter and Setter

- **get/setSource**  -> `String` value, default `null`<br />
Set the current media source (absolute path).

- **get/setColor**  -> `Color` value, default `#FFFFFF`<br />
Set the current foreground color.

- **get/setFontSize**  -> `float` value, default `11dp`<br />
Set the current font size.

- **get/setVolume**  -> `float` value, default `0.7`<br />
Set the current media player volume (0..1).

- **get/setPosition**  -> `int` value, default `0`<br />
Set the current media player position.

- **get/setPosition**  -> `int` value, default `0`<br />
Set the current media player position.

<br />

#### Interfaces

- **OnEventListener**<br />
**void onStartPlay(MediaPlayer player);**<br />
Called when the media start to play.<br />
**void onStopPlay();**<br />
Called when the media stop to play.<br />
<br />
<br />


# Usage

via Gradle:
<br />
Add it in your root build.gradle at the end of repositories:
```java
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

Add the dependency
```java
dependencies {
    ...
    compile 'com.github.paroca72:sc-player:1.0.0'
}
```

Coding
```java
// Get the components
ScPlayerButton player = (ScPlayerButton) this.findViewById(R.id.player);
assert player != null;

// Play the default ringtone
Uri defaultRintoneUri = RingtoneManager
        .getActualDefaultRingtoneUri(this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
player.setSource(defaultRintoneUri.toString());
```

<br />
<br />


# License
<pre>
 Copyright 2015 Samuele Carassai

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in  writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,  either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
</pre>
