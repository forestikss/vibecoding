package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.Environment;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.SocialInteractionsService;
import com.mojang.authlib.yggdrasil.response.BlockListResponse;
import com.mojang.authlib.yggdrasil.response.PrivilegesResponse;
import java.net.URL;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class YggdrasilSocialInteractionsService implements SocialInteractionsService {


   public YggdrasilSocialInteractionsService(YggdrasilAuthenticationService authenticationService, String accessToken, Environment env) throws AuthenticationException {

   }

   public boolean serversAllowed() {
      return true;
   }

   public boolean realmsAllowed() {
      return true;
   }

   public boolean chatAllowed() {
      return true;
   }

   public boolean isBlockedPlayer(UUID playerID) {
      return false;
   }
}
