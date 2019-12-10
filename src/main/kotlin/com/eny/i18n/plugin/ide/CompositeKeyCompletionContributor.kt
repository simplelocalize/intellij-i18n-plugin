package com.eny.i18n.plugin.ide

import com.eny.i18n.plugin.ide.settings.Settings
import com.eny.i18n.plugin.parser.FullKeyExtractor
import com.eny.i18n.plugin.tree.CompositeKeyResolver
import com.eny.i18n.plugin.tree.PsiElementTree
import com.eny.i18n.plugin.utils.JsonSearch
import com.eny.i18n.plugin.utils.unQuote
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement

class CompositeKeyCompletionContributor: CompletionContributor(), CompositeKeyResolver<PsiElement> {
    private val keyExtractor = FullKeyExtractor()
    private val DUMMY_KEY = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED

    private fun groupPlurals(set: Set<String>, pluralSeparator: String):List<String> =
        set.groupBy {it.substringBeforeLast(pluralSeparator)}
            .entries.flatMap {
                entry -> if(entry.value.size == 3 && entry.value.containsAll(listOf(1,2,5).map{entry.key+pluralSeparator+it})) {
                listOf(entry.key)} else entry.value
            }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
        val settings = Settings.getInstance(parameters.position.project)
        keyExtractor.extractI18nKeyLiteral(parameters.position)?.let {
            fullKey ->
                val fixedKey = fullKey.compositeKey.dropLast(1)
                fullKey.compositeKey.lastOrNull()?.let {
                    last ->
                        val search = last.text.let {value -> Regex(value.replace(DUMMY_KEY, ".*"))}
                        val source = fullKey.source.replace(last.text, "")
                        if (fullKey.ns != null) {
                            val elements =
                                groupPlurals(
                                    JsonSearch(parameters.position.project).findFilesByName(fullKey.ns.text).flatMap {
                                        file ->
                                            listCompositeKeyVariants(
                                                fixedKey,
                                                PsiElementTree.create(file),
                                                search
                                            )
                                        }.map {key -> key.value().text.unQuote()}.toSet(),
                                    settings.pluralSeparator
                                )
                            result.addAllElements(
                                elements.map {item -> LookupElementBuilder.create(source + item)}
                            )
                        }
                }
        }
    }
}