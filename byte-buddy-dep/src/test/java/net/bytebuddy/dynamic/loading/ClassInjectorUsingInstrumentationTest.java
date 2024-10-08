package net.bytebuddy.dynamic.loading;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.test.utility.AgentAttachmentRule;
import net.bytebuddy.utility.RandomString;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClassInjectorUsingInstrumentationTest {

    private static final String FOO = "foo", BAR = "bar";

    @Rule
    public MethodRule agentAttachmentRule = new AgentAttachmentRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File folder;

    @Before
    public void setUp() throws Exception {
        folder = temporaryFolder.newFolder();
    }

    @Test
    @AgentAttachmentRule.Enforce
    public void testBootstrapInjection() throws Exception {
        ClassInjector classInjector = ClassInjector.UsingInstrumentation.of(folder,
                ClassInjector.UsingInstrumentation.Target.BOOTSTRAP,
                ByteBuddyAgent.install());
        String name = FOO + RandomString.make();
        DynamicType dynamicType = new ByteBuddy().subclass(Object.class).name(name).make();
        Map<TypeDescription, Class<?>> types = classInjector.inject(Collections.singletonMap(dynamicType.getTypeDescription(), dynamicType.getBytes()));
        assertThat(types.size(), is(1));
        assertThat(types.get(dynamicType.getTypeDescription()).getName(), is(name));
        assertThat(types.get(dynamicType.getTypeDescription()).getClassLoader(), nullValue(ClassLoader.class));
    }

    @Test
    @AgentAttachmentRule.Enforce
    public void testSystemInjection() throws Exception {
        ClassInjector classInjector = ClassInjector.UsingInstrumentation.of(folder,
                ClassInjector.UsingInstrumentation.Target.SYSTEM,
                ByteBuddyAgent.install());
        String name = BAR + RandomString.make();
        DynamicType dynamicType = new ByteBuddy().subclass(Object.class).name(name).make();
        Map<TypeDescription, Class<?>> types = classInjector.inject(Collections.singletonMap(dynamicType.getTypeDescription(), dynamicType.getBytes()));
        assertThat(types.size(), is(1));
        assertThat(types.get(dynamicType.getTypeDescription()).getName(), is(name));
        assertThat(types.get(dynamicType.getTypeDescription()).getClassLoader(), is(ClassLoader.getSystemClassLoader()));
    }

    @Test
    @AgentAttachmentRule.Enforce
    public void testAvailable() {
        assertThat(ClassInjector.UsingInstrumentation.isAvailable(), is(true));
        assertThat(ClassInjector.UsingInstrumentation.of(folder,
                ClassInjector.UsingInstrumentation.Target.SYSTEM,
                ByteBuddyAgent.install()).isAlive(), is(true));
    }
}
