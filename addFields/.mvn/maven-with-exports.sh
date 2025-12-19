#!/bin/bash
export JAVA_HOME=/caminho/para/jdk-25
export PATH=$JAVA_HOME/bin:$PATH

# Adiciona os exports necessários para Javac API
export MAVEN_OPTS="\
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED"

# Executa Maven normalmente
mvn clean install "$@"
