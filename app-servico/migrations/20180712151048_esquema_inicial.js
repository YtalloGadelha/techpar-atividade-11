exports.up = knex => knex.schema.createTable("ordemServico", tb => {
    tb.increments("idOS")
    tb.string("funcionarioOS").notNullable()
    tb.string("descricaoOS").notNullable()
    tb.string("feedbackOS").notNullable()
  })
  
  exports.down = function(knex, Promise) {
    return knex.schema.dropTable("ordemServico")
  };