package com.example.detectordegestos.data.classifier

import android.content.Context
import org.json.JSONObject

class RandomForestClassifier(context: Context) {

    private data class DecisionTree(
        val childrenLeft: IntArray,
        val childrenRight: IntArray,
        val features: IntArray,
        val thresholds: DoubleArray,
        val values: Array<DoubleArray>
    )

    private val classes: List<String>
    private val featureCount: Int
    private val trees: List<DecisionTree>

    init {
        val jsonText = context.assets
            .open("models/random_forest.json")
            .bufferedReader()
            .use { it.readText() }

        val forest = JSONObject(jsonText)

        val classArray = forest.getJSONArray("classes")

        classes = List(classArray.length()) {
            classArray.getString(it)
        }

        featureCount = forest.getInt("n_features")

        val treeArray = forest.getJSONArray("trees")

        trees = List(treeArray.length()) { index ->
            val tree = treeArray.getJSONObject(index)

            fun intArray(key: String): IntArray {
                val array = tree.getJSONArray(key)
                return IntArray(array.length()) {
                    array.getInt(it)
                }
            }

            fun doubleArray(key: String): DoubleArray {
                val array = tree.getJSONArray(key)
                return DoubleArray(array.length()) {
                    array.getDouble(it)
                }
            }

            val valuesJson = tree.getJSONArray("value")

            val values = Array(valuesJson.length()) { node ->
                val probabilities = valuesJson.getJSONArray(node)

                DoubleArray(probabilities.length()) {
                    probabilities.getDouble(it)
                }
            }

            DecisionTree(
                childrenLeft = intArray("children_left"),
                childrenRight = intArray("children_right"),
                features = intArray("feature"),
                thresholds = doubleArray("threshold"),
                values = values
            )
        }

        require(trees.isNotEmpty()) {
            "O Random Forest não contém árvores."
        }
    }

    fun predict(features: FloatArray): String {
        require(features.size == featureCount) {
            "Esperadas $featureCount características, " +
                    "recebidas ${features.size}."
        }

        val probabilities = DoubleArray(classes.size)

        for (tree in trees) {
            var node = 0

            while (tree.childrenLeft[node] != -1) {
                val featureIndex = tree.features[node]

                node = if (
                    features[featureIndex].toDouble() <=
                    tree.thresholds[node]
                ) {
                    tree.childrenLeft[node]
                } else {
                    tree.childrenRight[node]
                }
            }

            val leafValues = tree.values[node]
            val total = leafValues.sum()

            if (total > 0.0) {
                for (i in classes.indices) {
                    probabilities[i] += leafValues[i] / total
                }
            }
        }

        val predictedIndex = probabilities.indices
            .maxByOrNull { probabilities[it] } ?: 0

        return classes[predictedIndex]
    }
}