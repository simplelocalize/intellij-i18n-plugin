package com.eny.i18n.plugin.ide.references

import com.eny.i18n.plugin.utils.unQuote
import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class ReferencesTestBase(private val ext: String) : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/resources/references"

    fun testReference() {
        myFixture.configureByFiles("jsx/testReference.jsx", "assets/test.$ext")
        val element = myFixture.file.findElementAt(myFixture.caretOffset)?.parent
        assertNotNull(element)
        assertEquals("Reference in json", element!!.references[0].resolve()?.text?.unQuote())
    }

    fun testPartiallyResolvedReference() {
        myFixture.configureByFiles("tsx/testPartiallyResolvedReference.tsx", "assets/test.$ext")
        val element = myFixture.file.findElementAt(myFixture.caretOffset)?.parent
        assertNotNull(element)
        assertEquals("section", element!!.references[0].resolve()?.text?.unQuote())
    }

    fun testExpressionReference() {
        myFixture.configureByFiles("tsx/testReference.tsx", "assets/test.$ext")
        val element = myFixture.file.findElementAt(myFixture.caretOffset)?.parent
        assertNotNull(element)
        assertEquals("section", element!!.references[0].resolve()?.text?.unQuote())
    }

    fun testInvalidTranslationRoot() {
        myFixture.configureByFiles("tsx/testInvalidReference.tsx", "assets/invalidRoot.$ext")
        val element = myFixture.file.findElementAt(myFixture.caretOffset)?.parent
        assertNotNull(element)
        assertEmpty(element!!.references)
    }

    fun testInvalidTranslationValue() {
        myFixture.configureByFiles("tsx/testInvalidTranslationValue.tsx", "assets/invalidTranslationValue.$ext")
        val element = myFixture.file.findElementAt(myFixture.caretOffset)?.parent
        assertNotNull(element)
        assertEquals("section", element!!.references[0].resolve()?.text?.unQuote())
    }
}

class ReferencesToJsonTranslationTest: ReferencesTestBase("json")
class ReferencesToYamlTranslationTest: ReferencesTestBase("yml")