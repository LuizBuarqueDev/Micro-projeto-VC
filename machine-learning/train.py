
from pathlib import Path

import joblib
import pandas as pd

from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix
)

BASE_DIR = Path(__file__).resolve().parent
FEATURES_DIR = BASE_DIR / "features"
MODELS_DIR = BASE_DIR / "models"

MODELS_DIR.mkdir(exist_ok=True)

# Carregar os conjuntos
train = pd.read_csv(FEATURES_DIR / "train.csv")
valid = pd.read_csv(FEATURES_DIR / "valid.csv")

# Separar características e classes
X_train = train.drop(columns=["classe"])
y_train = train["classe"]

X_valid = valid.drop(columns=["classe"])
y_valid = valid["classe"]

# Criar o modelo
modelo = RandomForestClassifier(
    n_estimators=100,
    max_depth=15,
    random_state=42,
    n_jobs=-1
)

# Treinamento
print("Treinando Random Forest...")
modelo.fit(X_train, y_train)

# Validação
previsoes = modelo.predict(X_valid)

print("\nAcurácia na validação:")
print(f"{accuracy_score(y_valid, previsoes):.2%}")

print("\nRelatório de classificação:")
print(classification_report(
    y_valid,
    previsoes,
    zero_division=0
))

print("\nMatriz de confusão:")
print(confusion_matrix(
    y_valid,
    previsoes,
    labels=modelo.classes_
))

print("\nOrdem das classes:")
print(modelo.classes_)

# Salvar modelo
caminho = MODELS_DIR / "random_forest.pkl"
joblib.dump(modelo, caminho)

print(f"\nModelo salvo em: {caminho}")
