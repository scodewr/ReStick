## @FIELDER

Uma anotação para gerar um novo atributo na classe em tempo de compilação. A princípio a ideia é conhecer melhor o processo de AST para gerar ao final do projeto a @Rehydrate estilo Lombok.

## Scripts para execução do Maven com parâmetros JVM especiais

### Contexto

Em Java 25+, alguns **Annotation Processors** que utilizam **APIs internas do Javac** (como `com.sun.tools.javac.api.JavacTrees`) não funcionam em módulos “unnamed” sem que a JVM receba parâmetros especiais:

```
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED
```

Sem esses parâmetros, ao compilar um módulo que usa o processor, você receberá o erro:

```
java.lang.IllegalAccessError: cannot access class com.sun.tools.javac.api.JavacTrees
```

Para contornar isso de forma confiável em **Maven**, criamos scripts que configuram a JVM corretamente antes de rodar o Maven.

---

### Scripts incluídos

#### Linux / macOS: `mvn-with-exports.sh`

```bash
#!/bin/bash
# Defina o JDK que será usado
export JAVA_HOME=/caminho/para/jdk-25
export PATH=$JAVA_HOME/bin:$PATH

# Configura os parâmetros de exportação para Javac API
export MAVEN_OPTS="\
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED"

# Executa Maven normalmente, repassando parâmetros extras
mvn clean install "$@"
```

#### Windows CMD: `mvn-with-exports.cmd`

```cmd
@echo off
REM Defina o JDK que será usado
set JAVA_HOME=C:\caminho\para\jdk-25
set PATH=%JAVA_HOME%\bin;%PATH%

REM Configura os parâmetros de exportação para Javac API
set MAVEN_OPTS=--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED

REM Executa Maven normalmente, repassando parâmetros extras
mvn clean install %*
```

---

### Como executar

#### Usando o script (recomendado)

* **Linux/macOS**

```bash
chmod +x mvn-with-exports.sh
./mvn-with-exports.sh
```

* **Windows CMD**

```cmd
mvn-with-exports.cmd
```

* Você pode passar parâmetros extras para Maven, por exemplo:

```bash
./mvn-with-exports.sh -pl fielder-app
```

---

#### Executando diretamente no terminal sem script

Se você quiser rodar sem usar o script, é necessário definir **MAVEN_OPTS** na mesma sessão do terminal:

* **Linux/macOS**

```bash
export MAVEN_OPTS="--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED"
mvn clean install
```

* **Windows CMD**

```cmd
set MAVEN_OPTS=--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED
mvn clean install
```

---