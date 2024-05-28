# java-opentelemetry-trancing-otlp
Esta aplicação java cria instrumentação manual do opentelemetry.

# Para Usar:

## Requisitos
* Java 8+
* Docker 16+


## 1 - Rode o colletor
Na pasta "colletor-otlp" execute o comando:

```shell script
docker run -p 4317:4317 -p 4318:4318 --rm -v $(pwd)/collector-config.yaml:/etc/otelcol/config.yaml otel/opentelemetry-collector
```


## 2 - Rode o App java-favorite
Use o maven para buildar ou se preferir rode diretamente na sua IDE:
