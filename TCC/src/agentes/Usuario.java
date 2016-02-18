/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;
import util.MersenneTwister;

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
                
                ServiceDescription servico = new ServiceDescription();
                servico.setType("chamada");
                
                //buscaServico(servico, "ligacao");
                //buscaServico(servico)[0].getName();
                //System.out.println((String)buscaServico(servico).get(0));
                //System.out.println((String)escolheOperadora(buscaServico(servico)).getName());
                contataOperadora(escolheOperadora(buscaServico(servico)));
            }
        }     
        
        recebeResposta();
        
        
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
    
    protected AID[] buscaServico(final ServiceDescription sd) {        
        
        AID[] operadoras;
                
        //addBehaviour(new OneShotBehaviour(this) {
            //@Override            
            //public void action() {
                
                DFAgentDescription dfd = new DFAgentDescription();
                dfd.addServices(sd);
                
                try {
                    
                    DFAgentDescription[] resultado = DFService.search(this, dfd);
                    if(resultado.length != 0) {
                        
                        operadoras = new AID[resultado.length];
                        for (int i = 0; i < resultado.length; ++i) {
                            operadoras[i] = resultado[i].getName();
                        }
                        return operadoras;
                    }
                }
                catch(FIPAException e) {
                    e.printStackTrace();
                }
            //}           
        //});
        recebeResposta();
        return null;
        
    } //Fim do método buscaServico
    
    protected AID escolheOperadora(AID[] operadoras) {
        
        int seed = operadoras.length;
        Random random = new Random();
        AID ope = (AID) operadoras[random.nextInt(seed)];
        
        return ope;
    }
    
    protected void contataOperadora(final AID operadora) {
        
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
               
                if(operadora != null) {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(operadora);
                    msg.setContent("ligacao");
                    send(msg);                    
                    
                }                 
            }           
            
        });
    } //Fim do método contataOperadora
    
    protected void recebeResposta() {
        
        addBehaviour(new CyclicBehaviour(this) {
            
            public void action() {
                
                ServiceDescription servico = new ServiceDescription();
                servico.setType("chamada");
                
                ACLMessage msg = receive();
                if(msg != null) {
                    if(msg.getContent().equalsIgnoreCase("Rede ocupada")) { //Se a requisição foi rejeitada, tentará com outra operadora
                        contataOperadora(escolheOperadora(buscaServico(servico)));
                        System.out.println("Vazio");
                    } else {tempoDeVida();}
                }
            
            }
        });
    } //Fim do método recebeResposta
    
    protected void tempoDeVida() {
        
        final MersenneTwister duracaoChamada = new MersenneTwister(System.currentTimeMillis());        
        
        addBehaviour(new Behaviour(this) {
            boolean status = false;
            @Override
            public void action() {          
                
                try {
                    
                    myAgent.doSuspend();
                    Thread.sleep(Math.abs(duracaoChamada.nextShort()));                                        
                    status = true;
                    myAgent.doActivate();
                    // PENDÊNCIA: Liberar o canal que estava sendo utilizado na Operadora
                    
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }               
            }
            
            @Override
            public boolean done() {
                
                return status;
            }
            
            public int onEnd() {
                
                System.out.println("Chamada encerrada");
                terminaUsuario();
                return 0;           
            }
        });       
    }
    
    protected void terminaUsuario() {
        
        addBehaviour(new Behaviour(this) {
            
            boolean status = false;
            
            public void action() {          
                
                // PENDÊNCIA: Verificar se o tempo total da simulação já acabou e tomar as ações devidas
                
                myAgent.doDelete();
            }
            
            @Override
            public boolean done() {
                
                return status;
            }
            
            public int onEnd() {
                
                System.out.println("O agente " + myAgent.getLocalName() + " foi encerrado");
                return 0;           
            }
        });
        
        
    }
    
} //Fim da classe Usuario
