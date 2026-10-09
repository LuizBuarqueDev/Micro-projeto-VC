# Detector de Gestos — Pedra, Papel e Tesoura

Desenvolvi uma aplicação móvel que demonstra o uso de visão computacional e aprendizado de máquina para reconhecer gestos das mãos em tempo real. Meu objetivo foi explorar soluções que possam melhorar a acessibilidade e a interação com aplicativos. Para simplificar o desenvolvimento, escolhi os gestos Pedra, Papel e Tesoura como demonstração prática dessa tecnologia.

## Etapas do desenvolvimento

1. **Dataset:** obtive as imagens do Rock Paper Scissors (Roboflow) e organizei nas pastas `dataset/train`, `dataset/valid` e `dataset/test`, com as classes `paper`, `rock` e `scissors`.

2. **Extração de características:** utilizei um script Python com MediaPipe Hand Landmarker (`hand_landmarker.task`) para identificar 21 pontos da mão. Centralizei as coordenadas X, Y e Z no punho e normalizei os valores, obtendo 63 características por imagem. Salvei os dados em `features/train.csv`, `features/valid.csv` e `features/test.csv`.

3. **Treinamento:** treinei um Random Forest com os dados de `train.csv`, utilizando 100 árvores e profundidade máxima de 15. Salvei o modelo em `models/random_forest.pkl`.

4. **Validação:** avaliei o modelo com `valid.csv` e obtive **92,45% de acurácia** e **0,9235 de F1 Macro** em 371 amostras.

5. **Exportação:** converti o modelo `.pkl` para `random_forest.json`, contendo as classes e as estruturas das árvores de decisão, para utilizá-lo no Android.

6. **Desenvolvimento mobile:** criei o aplicativo no Android Studio, utilizando Kotlin, Jetpack Compose e CameraX. Implementei a captura dos quadros pela câmera, a detecção dos pontos da mão pelo MediaPipe e a classificação dos gestos pelo Random Forest. Também adicionei a opção de mostrar ou ocultar os pontos detectados.

7. **Testes finais:** avaliei 30 imagens de teste. O MediaPipe detectou 28 mãos, das quais 27 foram classificadas corretamente. Obtive **96,43% de acurácia nas imagens detectadas** e **90% de acerto geral**.

## Tecnologias

Utilizei Python, MediaPipe, OpenCV, NumPy, Pandas, scikit-learn, Kotlin, Jetpack Compose e CameraX.

## Como utilizar

Para executar o aplicativo, é necessário abrir o projeto no Android Studio, sincronizar as dependências e executá-lo em um dispositivo com câmera. Após conceder a permissão, basta posicionar a mão diante da câmera e realizar um dos três gestos. O reconhecimento utiliza os modelos já incluídos no aplicativo, sem necessidade de novo treinamento.

## Limitações

Observei que fatores como iluminação, posição da mão e diferenças entre as imagens do dataset e as condições reais podem afetar a detecção e a classificação dos gestos.
