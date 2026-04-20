package com.mojang.authlib.minecraft;

import com.mojang.authlib.AuthenticationService;
import lombok.Getter;

@Getter
public abstract class BaseMinecraftSessionService implements MinecraftSessionService {
   private final AuthenticationService authenticationService;

   protected BaseMinecraftSessionService(AuthenticationService authenticationService) {
      this.authenticationService = authenticationService;
   }

}
