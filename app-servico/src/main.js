const cfg = require("../knexfile")
let env = process.env.NODE_ENV || "development"
const knex = require("knex")(cfg[env])
const express = require("express")
const morgan = require("morgan")
const bodyParser = require("body-parser")
const app = express()
const cors = require("cors")

app.use(cors())
app.use(morgan("dev"))
app.use(bodyParser.json())
app.use(bodyParser.raw({
  type: ['application/octet-stream', 'image/*', 'application/pdf', 'audio/*'],
  limit: 10240 * 1024 // ~10mb
}));

app.get("/list", (req, res) => {
    knex("ordemServico").where("feedbackOs", "").select().then(ret => {
      res.send(ret)
    }).catch(err => {
      res.status(500).send(err)
      console.log(err)
    })
})

app.post("/save", (req, res) => {
  const mime = req.header("Content-Type")
  const nome = req.header("X-Filename")
  knex("midiaOS").insert({
    nomemidiaOS:nome,
    mimemidiaOS:mime,
    payloadmidiaOS:req.body
  },"idmidiaOS").then(ret => {
      res.send({
      idmidiaOS:ret[0]
      })
    }).catch(err => {
      res.status(500).send(err)
      console.log(err)
    })
})

app.put("/save", (req, res) => {
  const servico = req.body
  knex("ordemServico").where("idOS", servico.idOS).update(servico).then(ret => {
      res.send(ret)
  }).catch(err => {
    res.status(500).send(err)
    console.log(err)
  })
})

knex.migrate.latest().then(_ =>
  app.listen(3000, _ =>
console.log("Servidor online!")))