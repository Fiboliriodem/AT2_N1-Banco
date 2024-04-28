import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Banco {
    private final Lock lock = new ReentrantLock();

    public void transferir(Conta origem, Conta destino, double valor) {
        lock.lock();
        try {
            if (origem.getSaldo() >= valor) {
                origem.debitar(valor);
                destino.creditar(valor);
                System.out.println("Transferência de R$ " + valor + " realizada de " + origem.getNome() +
                        " para " + destino.getNome());
            } else {
                System.out.println("Saldo insuficiente em " + origem.getNome() + " para transferência de R$ " + valor);
                // Retorna sem executar a transferência se o saldo for insuficiente
            }
        } finally {
            lock.unlock();
        }
    }
}




class Loja {
    Conta conta;

    public Loja(Conta conta) {
        this.conta = conta;
    }

}

class Funcionario extends Thread {
    private final String nome;
    private final Conta contaSalario;
    private final Conta contaInvestimento;

    public Funcionario(String nome, Conta contaSalario, Conta contaInvestimento) {
        this.nome = nome;
        this.contaSalario = contaSalario;
        this.contaInvestimento = contaInvestimento;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            synchronized (contaSalario) {
                double salario = contaSalario.getSaldo();
                double percentualInvestimento = 0.2;
                double valorInvestimento = salario * percentualInvestimento;
                if (salario >= valorInvestimento) {
                    contaSalario.debitar(valorInvestimento);
                    contaInvestimento.creditar(valorInvestimento);
                    System.out.println(nome + " investiu R$ " + valorInvestimento + " do salário em investimentos");
                } else {
                    System.out.println("Saldo insuficiente em " + contaSalario.getNome() + " para investimento de R$ " + valorInvestimento);
                }
            }
            try {
                Thread.sleep(1000); // Tempo de espera entre os investimentos
            } catch (InterruptedException e) {
                // A exceção InterruptedException será lançada quando a thread for interrompida
                // Podemos sair do loop neste caso
                break;
            }
        }
    }
}

class Cliente extends Thread {
    private final String nome;
    final Conta conta;
    private boolean continuarComprando;

    public Cliente(String nome, Conta conta) {
        this.nome = nome;
        this.conta = conta;
        this.continuarComprando = true;
    }

    public void pararCompras() {
        continuarComprando = false;
    }

    @Override
    public void run() {
        while (continuarComprando) {
            double valorCompra = Math.random() < 0.5 ? 100 : 200;
            synchronized (conta) {
                if (conta.getSaldo() >= valorCompra) {
                    conta.debitar(valorCompra);
                    System.out.println(nome + " comprou um item de R$ " + valorCompra);
                } else {
                    System.out.println("Saldo insuficiente para comprar um item de R$ " + valorCompra + " para " + nome);
                    pararCompras(); // Parar compras se saldo for insuficiente
                }
            }
            try {
                Thread.sleep(1000); // Tempo entre as compras
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



class Conta {
    private final String nome;
    private double saldo;

    public Conta(String nome, double saldo) {
        this.nome = nome;
        this.saldo = saldo;
    }

    public double getSaldo() {
        return saldo;
    }

    public String getNome() {
        return nome;
    }

    public synchronized void creditar(double valor) {
        saldo += valor;
    }

    public synchronized void debitar(double valor) {
        saldo -= valor;
    }
}

public class Main {
    public static void main(String[] args) {
        Banco banco = new Banco();

        Loja loja1 = new Loja(new Conta("Loja1", 0));
        Loja loja2 = new Loja(new Conta("Loja2", 0));

        Funcionario func1Loja1 = new Funcionario("Funcionario1Loja1", loja1.conta, new Conta("Investimento1Loja1", 0));
        Funcionario func2Loja1 = new Funcionario("Funcionario2Loja1", loja1.conta, new Conta("Investimento2Loja1", 0));
        Funcionario func1Loja2 = new Funcionario("Funcionario1Loja2", loja2.conta, new Conta("Investimento1Loja2", 0));
        Funcionario func2Loja2 = new Funcionario("Funcionario2Loja2", loja2.conta, new Conta("Investimento2Loja2", 0));

        Cliente cliente1 = new Cliente("Cliente1", new Conta("ContaCliente1", 1000));
        Cliente cliente2 = new Cliente("Cliente2", new Conta("ContaCliente2", 1000));
        Cliente cliente3 = new Cliente("Cliente3", new Conta("ContaCliente3", 1000));
        Cliente cliente4 = new Cliente("Cliente4", new Conta("ContaCliente4", 1000));
        Cliente cliente5 = new Cliente("Cliente5", new Conta("ContaCliente5", 1000));

        func1Loja1.start();
        func2Loja1.start();
        func1Loja2.start();
        func2Loja2.start();
        cliente1.start();
        cliente2.start();
        cliente3.start();
        cliente4.start();
        cliente5.start();

        // Thread para realizar as transferências de contas
        Thread transferencias = new Thread(() -> {
            while (true) {
                banco.transferir(cliente1.conta, loja1.conta, 100);
                banco.transferir(cliente2.conta, loja2.conta, 200);
                banco.transferir(cliente3.conta, loja1.conta, 200);
                banco.transferir(cliente4.conta, loja2.conta, 100);
                banco.transferir(cliente5.conta, loja1.conta, 100);
            }
        });
        transferencias.start();
    }
}
