/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
/**
 *
 * @author Allef
 */
public class Usuario extends Agent {
    
    protected void setup(){
        
        //Obtem argumentos
        Object[] args = getArguments();
        if(args != null && args.length > 0) {
            String argumento = (String) args[0];
            
            if(argumento.equalsIgnoreCase("ligacao")) {
                ServiceDescription servico =  new ServiceDescription();
                //O serviço é "chamada"
                servico.setType("chamada");
                buscaServico(servico, "ligacao");
            }
        }      
        
        
        
    } //Fim do método setup
    
    protected void buscaServico(final ServiceDescription sd, final String Pedido) {
        
        //A cada meio minuto busca por agentes que fornecem o serviço especificado
        addBehaviour(new TickerBehaviour(this, 30000) {
            @Override
            protected void onTick() {
                DFAgentDescription dfd = new DFAgentDescription();
                dfd.addServices(sd);
                try {
                    DFAgentDescription[] resultado = DFService.search(myAgent, dfd);
                    if(resultado.length != 0) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(resultado[0].getName());
                        msg.setContent(Pedido);
                        myAgent.send(msg);
                        stop();
                    } 
                }
                catch(FIPAException e) {
                    e.printStackTrace();
                }
            }
        });
    } //Fim do método buscaServico
    
    protected void recebeResposta(final String mensagem) {
        
        addBehaviour(new CyclicBehaviour(this) {
            
            public void action() {
                
                ACLMessage msg = receive();
                if(msg != null) {
                    if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) { //Se a requisição foi rejeitada, tentará com outra operadora
                        //escolheOperadora();
                    } else {
                        block();
                    }
                }
            
            }
        });
    } //Fim do método recebeResposta
    
} //Fim da classe Usuario
