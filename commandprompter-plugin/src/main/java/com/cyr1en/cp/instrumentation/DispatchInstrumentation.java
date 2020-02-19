/*
 * MIT License
 *
 * Copyright (c) 2020 Ethan Bacurio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cyr1en.cp.instrumentation;

import com.cyr1en.cp.CommandPrompter;
import com.cyr1en.javen.Dependency;
import com.cyr1en.javen.Javen;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.command.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public class DispatchInstrumentation {

  private final String DELEGATE_NAME = "commandprompter-delegate.jar";
  private final Dependency DELEGATE_JAR = new Dependency("github.com.cyr1en", "commandprompter-delegate", "");

  private CommandPrompter instance;
  private Javen javen;

  public DispatchInstrumentation(CommandPrompter instance) {
    this.javen = new Javen(Paths.get(instance.getDataFolder().getAbsolutePath() + "/libs"));
    //Attach API will be provided by Javen.
    ByteBuddyAgent.install(ByteBuddyAgent.AttachmentProvider.ForUserDefinedToolsJar.INSTANCE);
    this.instance = instance;
    prepareDelegate();
  }

  private void prepareDelegate() {
    try{
      javen.getDownloader().downloadJar(DELEGATE_JAR);
      JarFile jarFile = new JarFile(String.valueOf(Paths.get(javen.getLibsDir().getAbsolutePath(), DELEGATE_NAME)));
      DispatchDelegator.init(Delegate::dispatch);
      ByteBuddyAgent.getInstrumentation().appendToBootstrapClassLoaderSearch(jarFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void instrument() {
    instance.getLogger().info("Instrumenting SimpleCommandMap...");
    new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V8))
            .redefine(SimpleCommandMap.class)
            .method(ElementMatchers.named("dispatch")
                    .and(ElementMatchers.isDeclaredBy(SimpleCommandMap.class))
                    .and(ElementMatchers.canThrow(CommandException.class))
                    .and(ElementMatchers.returns(boolean.class)))
            .intercept(MethodDelegation.to(DispatchDelegator.class))
            .make()
            .load(ByteBuddy.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    instance.getLogger().info("\u001b[32mSuccessfully delegated dispatch method in SimpleCommandMap.\033[0m");
  }
}
