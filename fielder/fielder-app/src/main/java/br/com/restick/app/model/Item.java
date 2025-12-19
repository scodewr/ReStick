package br.com.restick.app.model;

import br.com.restick.api.annotation.Fielder;

@Fielder(name = "value", type = Long.class)
public class Item {

    public String name;

    @Fielder(name = "Teste") // Não produz efeito
    public enum type {
        PRODUCT
    }

    /*
     Remover o comentário reproduz erro de compilação devido à falha na regra da anotação.
     Veja FielderValidator.
        public Item(String name){
            this.name = name;
        }
    */

}
