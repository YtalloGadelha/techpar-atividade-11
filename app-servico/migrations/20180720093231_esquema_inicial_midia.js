exports.up = knex => knex.schema.createTable("midiaOS", tb => {
    tb.increments("idmidiaOS")
    tb.binary("payloadmidiaOS").notNullable()
    tb.string("nomemidiaOS").defaultTo("semnome.png")
    tb.string("mimemidiaOS").defaultTo("imagem/png")
    tb.string("cdnurlmidiaOS");
  })
  
  exports.down = knex => knex.schema.dropTable("midiaOS")