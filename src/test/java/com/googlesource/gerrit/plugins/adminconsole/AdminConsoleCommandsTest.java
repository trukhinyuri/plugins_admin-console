// Copyright (C) 2026 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.adminconsole;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gerrit.entities.Account;
import com.google.gerrit.entities.Project;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.AccountCache;
import com.google.gerrit.server.account.AccountResolver;
import com.google.gerrit.server.account.AccountState;
import com.google.gerrit.server.account.externalids.ExternalIds;
import com.google.gerrit.server.git.meta.MetaDataUpdate;
import com.google.gerrit.server.project.ProjectConfig;
import com.google.gerrit.server.restapi.account.GetGroups;
import com.google.gerrit.server.restapi.account.GetSshKeys;
import com.google.gerrit.sshd.SshCommand;
import com.google.inject.util.Providers;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import org.junit.Test;

public class AdminConsoleCommandsTest {

  @Test
  public void showRepoAccessClosesMetaDataUpdate() throws Exception {
    MetaDataUpdate.Server mdFactory = mock(MetaDataUpdate.Server.class);
    MetaDataUpdate md = mock(MetaDataUpdate.class);
    ProjectConfig.Factory cfgFactory = mock(ProjectConfig.Factory.class);
    ProjectConfig cfg = mock(ProjectConfig.class);
    Project.NameKey project = Project.nameKey("All-Projects");

    when(mdFactory.create(project)).thenReturn(md);
    when(cfgFactory.read(md)).thenReturn(cfg);
    when(cfg.getAccessSections()).thenReturn(ImmutableList.of());

    ShowRepoAccessCommand cmd = new ShowRepoAccessCommand(mdFactory, cfgFactory);
    setField(ShowRepoAccessCommand.class, cmd, "projectName", "All-Projects");
    StringWriter out = new StringWriter();
    setField(SshCommand.class, cmd, "stdout", new PrintWriter(out, true));

    cmd.run();

    verify(md).close();
  }

  @Test
  public void showAccountHandlesEmptyUserNameWithoutThrowing() throws Exception {
    Account.Id accountId = Account.id(1000);
    Account account = Account.builder(accountId, Instant.EPOCH).setFullName("Test User").build();
    AccountState state = AccountState.forAccount(account);

    AccountResolver resolver = mock(AccountResolver.class);
    AccountResolver.Result result = mock(AccountResolver.Result.class);
    when(resolver.resolve("1000")).thenReturn(result);
    when(result.asIdSet()).thenReturn(ImmutableSet.of(accountId));
    when(result.asUnique()).thenReturn(state);

    AccountCache accountCache = mock(AccountCache.class);
    when(accountCache.get(accountId)).thenReturn(Optional.of(state));

    ExternalIds externalIds = mock(ExternalIds.class);
    when(externalIds.byAccount(accountId)).thenReturn(ImmutableSet.of());

    ShowAccountCommand cmd =
        new ShowAccountCommand(
            resolver,
            Providers.of(mock(GetGroups.class)),
            mock(IdentifiedUser.GenericFactory.class),
            Providers.of(mock(GetSshKeys.class)),
            externalIds,
            accountCache);
    setField(ShowAccountCommand.class, cmd, "name", "1000");
    StringWriter out = new StringWriter();
    setField(SshCommand.class, cmd, "stdout", new PrintWriter(out, true));

    cmd.run();

    assertThat(out.toString()).contains("User Name:         ");
  }

  private static void setField(Class<?> clazz, Object instance, String name, Object value)
      throws Exception {
    Field field = clazz.getDeclaredField(name);
    field.setAccessible(true);
    field.set(instance, value);
  }
}
