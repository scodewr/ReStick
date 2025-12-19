package br.com.restick.app.model;

import br.com.restick.api.annotation.AddFields;
import br.com.restick.api.annotation.FieldDef;

@AddFields({
        @FieldDef(name = "model", type = String.class, modifier = 1L),
        @FieldDef(name = "color", type = String.class,  modifier = 1L),
        @FieldDef(name = "type", type = String.class,  modifier = 1L),
        @FieldDef(name = "value", type = Long.class,  modifier = 1L)
})
public class Car {
}
