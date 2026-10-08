
from pathlib import Path

import cv2
import mediapipe as mp
import numpy as np
import pandas as pd


# Diretórios do projeto
BASE_DIR = Path(__file__).resolve().parent
DATASET_DIR = BASE_DIR / "dataset"
MODEL_PATH = BASE_DIR / "hand_landmarker.task"
OUTPUT_DIR = BASE_DIR / "features"

CLASSES = ["paper", "rock", "scissors"]
DIVISOES = ["train", "valid", "test"]

EXTENSOES = {".jpg", ".jpeg", ".png", ".webp"}

# MediaPipe Tasks API
BaseOptions = mp.tasks.BaseOptions
HandLandmarker = mp.tasks.vision.HandLandmarker
HandLandmarkerOptions = mp.tasks.vision.HandLandmarkerOptions
RunningMode = mp.tasks.vision.RunningMode


def extrair_features(imagem, detector):
    # OpenCV utiliza BGR; MediaPipe espera RGB
    imagem_rgb = cv2.cvtColor(
        imagem, cv2.COLOR_BGR2RGB
    )

    mp_image = mp.Image(
        image_format=mp.ImageFormat.SRGB,
        data=imagem_rgb
    )

    resultado = detector.detect(mp_image)

    if not resultado.hand_landmarks:
        return None

    # Primeira mão detectada
    mao = resultado.hand_landmarks[0]

    # 21 pontos com coordenadas x, y, z
    pontos = np.array(
        [[p.x, p.y, p.z] for p in mao],
        dtype=np.float32
    )

    # Centralizar os pontos no punho (ponto 0)
    pontos -= pontos[0].copy()

    # Normalizar pelo tamanho da mão
    escala = np.max(
        np.linalg.norm(pontos[:, :2], axis=1)
    )

    if escala < 1e-6:
        return None

    pontos /= escala

    # Transformar 21 x 3 em 63 características
    return pontos.flatten().tolist()


def processar_dataset():
    OUTPUT_DIR.mkdir(exist_ok=True)

    opcoes = HandLandmarkerOptions(
        base_options=BaseOptions(
            model_asset_path=str(MODEL_PATH)
        ),
        running_mode=RunningMode.IMAGE,
        num_hands=1,
        min_hand_detection_confidence=0.5
    )

    colunas = [
        f"{eixo}{i}"
        for i in range(21)
        for eixo in ("x", "y", "z")
    ] + ["classe"]

    with HandLandmarker.create_from_options(
        opcoes
    ) as detector:

        for divisao in DIVISOES:
            registros = []
            total = 0
            falhas = 0

            print(f"\nProcessando: {divisao}")

            for classe in CLASSES:
                pasta = DATASET_DIR / divisao / classe

                if not pasta.is_dir():
                    raise FileNotFoundError(
                        f"Pasta não encontrada: {pasta}"
                    )

                arquivos = sorted(
                    arquivo
                    for arquivo in pasta.rglob("*")
                    if arquivo.suffix.lower() in EXTENSOES
                )

                print(
                    f"  {classe}: {len(arquivos)} imagens"
                )

                for arquivo in arquivos:
                    total += 1

                    imagem = cv2.imread(str(arquivo))

                    if imagem is None:
                        falhas += 1
                        continue

                    features = extrair_features(
                        imagem, detector
                    )

                    if features is None:
                        falhas += 1
                        continue

                    registros.append(
                        features + [classe]
                    )

            df = pd.DataFrame(
                registros,
                columns=colunas
            )

            destino = OUTPUT_DIR / f"{divisao}.csv"

            df.to_csv(
                destino,
                index=False
            )

            print(f"Total de imagens: {total}")
            print(f"Mãos detectadas: {len(df)}")
            print(f"Falhas: {falhas}")
            print(f"Salvo em: {destino}")


if __name__ == "__main__":
    processar_dataset()