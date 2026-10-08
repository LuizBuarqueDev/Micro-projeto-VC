
import json
import joblib
from pathlib import Path

MODEL_PATH = Path("models/random_forest.pkl")
OUTPUT_PATH = Path("random_forest.json")

model = joblib.load(MODEL_PATH)

forest = {
    "classes": model.classes_.tolist(),
    "n_features": int(model.n_features_in_),
    "trees": []
}

for estimator in model.estimators_:
    tree = estimator.tree_

    forest["trees"].append({
        "children_left": tree.children_left.tolist(),
        "children_right": tree.children_right.tolist(),
        "feature": tree.feature.tolist(),
        "threshold": tree.threshold.tolist(),
        "value": tree.value[:, 0, :].tolist()
    })

with open(OUTPUT_PATH, "w", encoding="utf-8") as file:
    json.dump(forest, file, separators=(",", ":"))

print("Modelo exportado com sucesso!")
print("Classes:", forest["classes"])
print("Características:", forest["n_features"])
print("Árvores:", len(forest["trees"]))
print("Arquivo:", OUTPUT_PATH.resolve())
