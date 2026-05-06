package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestRunConfigurationFactoryTest : BasePlatformTestCase() {

    fun testRunConfigurationType_id() {
        val type = GoTestRunConfigurationType()
        assertEquals("GoTestRunConfiguration", type.id)
    }

    fun testRunConfigurationType_displayName() {
        val type = GoTestRunConfigurationType()
        assertEquals("Go Test", type.displayName)
    }

    fun testRunConfigurationType_hasExactlyOneFactory() {
        val type = GoTestRunConfigurationType()
        assertEquals(1, type.configurationFactories.size)
    }

    fun testRunConfigurationType_factoryIsGoTestRunConfigurationFactory() {
        val type = GoTestRunConfigurationType()
        assertTrue(type.configurationFactories[0] is GoTestRunConfigurationFactory)
    }

    fun testRunConfigurationFactory_getId() {
        val type = GoTestRunConfigurationType()
        val factory = type.configurationFactories[0] as GoTestRunConfigurationFactory
        assertEquals("GoTestRunConfigurationFactory", factory.getId())
    }

    fun testRunConfigurationFactory_createTemplateConfiguration_returnsGoTestRunConfiguration() {
        val type = GoTestRunConfigurationType()
        val factory = type.configurationFactories[0] as GoTestRunConfigurationFactory
        val config = factory.createTemplateConfiguration(project)
        assertTrue(config is GoTestRunConfiguration)
    }

    fun testRunConfigurationFactory_createTemplateConfiguration_hasExpectedName() {
        val type = GoTestRunConfigurationType()
        val factory = type.configurationFactories[0] as GoTestRunConfigurationFactory
        val config = factory.createTemplateConfiguration(project)
        assertEquals("Go Test", config.name)
    }

    fun testRunConfigurationFactory_createTemplateConfiguration_hasDefaultTestKind() {
        val type = GoTestRunConfigurationType()
        val factory = type.configurationFactories[0] as GoTestRunConfigurationFactory
        val config = factory.createTemplateConfiguration(project) as GoTestRunConfiguration
        assertEquals(GoTestKind.TEST, config.testKind)
    }

    fun testRunConfigurationFactory_createTemplateConfiguration_hasDefaultScope() {
        val type = GoTestRunConfigurationType()
        val factory = type.configurationFactories[0] as GoTestRunConfigurationFactory
        val config = factory.createTemplateConfiguration(project) as GoTestRunConfiguration
        assertEquals(GoTestScope.DIRECTORY, config.scope)
    }
}
