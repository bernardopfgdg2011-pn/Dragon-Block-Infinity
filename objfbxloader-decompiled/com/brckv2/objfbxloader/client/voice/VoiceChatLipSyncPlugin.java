package com.brckv2.objfbxloader.client.voice;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent;

public final class VoiceChatLipSyncPlugin implements VoicechatPlugin {
   public String getPluginId() {
      return "objfbxloader_lipsync";
   }

   public void registerEvents(EventRegistration registration) {
      VoiceLipSyncState.initialize();
      registration.registerEvent(ClientSoundEvent.class, this::onClientSoundEvent);
      registration.registerEvent(MicrophoneMuteEvent.class, this::onMicrophoneMuteEvent);
   }

   private void onClientSoundEvent(ClientSoundEvent event) {
      VoiceLipSyncState.onClientAudioFrame(event.getRawAudio());
   }

   private void onMicrophoneMuteEvent(MicrophoneMuteEvent event) {
      VoiceLipSyncState.onMicrophoneMuted(event.isDisabled());
   }
}
