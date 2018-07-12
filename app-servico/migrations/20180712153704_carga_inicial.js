const servicos = [

    {idOS: 1, funcionarioOS: "João", descricaoOS:"Trocar sensor da luz", feedbackOS: ""},
    {idOS: 2, funcionarioOS: "José", descricaoOS:"Trocar sensor do vento", feedbackOS: ""},
    {idOS: 3, funcionarioOS: "Ronaldo", descricaoOS:"Tirar foto do datalogger", feedbackOS: ""},
    {idOS: 4, funcionarioOS: "Leonardo", descricaoOS:"Trocar antena tal!", feedbackOS: ""},
    {idOS: 5, funcionarioOS: "João", descricaoOS:"Verificar sensor do vento!", feedbackOS: ""},
    {idOS: 6, funcionarioOS: "José", descricaoOS:"Verificar sensor da luz!", feedbackOS: ""},
    {idOS: 7, funcionarioOS: "Ronaldo", descricaoOS:"Verificar posição da antena!", feedbackOS: ""},
    {idOS: 8, funcionarioOS: "Leonardo", descricaoOS:"Coletar dados da torre 10!", feedbackOS: ""}
]  

exports.up = knex => knex("ordemServico").insert(servicos)

exports.down = knex => knex("ordemServico").del()
  .whereIn("idOS", servicos.map(e => e.idOS))