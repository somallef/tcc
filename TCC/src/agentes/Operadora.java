/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import util.MersenneTwister;

/*
 *
 * @author Allef
 */
public class Operadora extends Agent {

    private Boolean canais[] = new Boolean[2];
    
    /*   
    public Operadora(int qtdCanais) {
        
        this.canais = new Boolean[qtdCanais];   
    }
    */    
    protected void setup(){
        
        canais[0] = true;
        canais[1] = true;
        
        //Criamos uma entrada no Directory Facilitator
        DFAgentDescription dfd = new DFAgentDescription();
        //Informamos a ID do Agente
        dfd.setName(getAID());
        
        //Criamos um serviço
        ServiceDescription sd = new ServiceDescription();
        //Informamos um tipo para o serviço
        sd.setType("chamada");
        //Informamos um nome para o serviço
        sd.setName(this.getLocalName());
        //Adicionamos o serviço à descrição
        dfd.addServices(sd);
        
        //Adicionamos o agente ao DF
        try{
            
            //register(agente que oferece o serviço, descrição)
            DFService.register(this, dfd);
            
        }catch (FIPAException e){
            
            e.printStackTrace();
            
        }
        
        recebeRequisicao("ligacao");
            
    } //Fim do método setup
    
    protected void recebeRequisicao(final String mensagem) {
        
        addBehaviour(new CyclicBehaviour(this) {
            
            public void action() {
                
                ACLMessage msg = receive();
                if(msg != null) {
                    if(msg.getContent().equalsIgnoreCase(mensagem)) {
                        ACLMessage reply = msg.createReply();
                        loop:
                        for(int i = 0; i < canais.length; i++) {
                            if(canais[i]) {
                                canais[i] = false;
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                reply.setContent("Alocado no canal " + i);
                                break loop;                                
                            } else {    
                                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                reply.setContent("Rede ocupada");                                                             
                            }                            
                        }
                        myAgent.send(reply);
                        
                                               
                    }
                }
            
            }
        });
    } //Fim do método recebeRequisicao
    
    protected void takeDown() {
        
        /* 
        ** É uma boa prática remover o registro do agente no DF assim que a sua 
        ** execução é terminada, pois isto não ocorre automaticamente. Isto deve 
        ** ser feito na implementação do método takeDown.
        */
        
        //Removemos o agente do DF
        try{
            
            DFService.deregister(this);
            
        }catch (FIPAException e){
            
            e.printStackTrace();
            
        }
        
    } //Fim do método takeDown  
    
} //Fim da classe Operadora
