import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Conta {
    private double saldo;
    private final Lock lock = new ReentrantLock();

    public Conta(double saldo) {
        this.saldo = saldo;
    }

    public double getSaldo() {
        return saldo;
    }

    public void depositar(double valor) {
        lock.lock();
        try {
            saldo += valor;
        } finally {
            lock.unlock();
        }
    }

    public void sacar(double valor) throws SaldoInsuficienteException {
        lock.lock();
        try {
            if (saldo >= valor) {
                saldo -= valor;
            } else {
                throw new SaldoInsuficienteException("Saldo insuficiente para saque.");
            }
        } finally {
            lock.unlock();
        }
    }

    class SaldoInsuficienteException extends Exception {
        public SaldoInsuficienteException(String mensagem) {
            super(mensagem);
        }
    }
}

class Cliente extends Thread {
    private static final int NUMERO_COMPRAS = 10; // Número de compras que cada cliente fará
    private Conta conta;

    public Cliente(Conta conta) {
        this.conta = conta;
    }

    public void run() {
        try {
            for (int i = 0; i < NUMERO_COMPRAS; i++) {
                double valorCompra = Math.random() < 0.5 ? 100 : 200;
                if (conta.getSaldo() >= valorCompra) {
                    conta.sacar(valorCompra);
                    // Simulação de compra na loja
                    System.out.println("Cliente realizou uma compra de $" + valorCompra);
                    Thread.sleep((long) (Math.random() * 1000)); // Tempo de compra simulado
                } else {
                    System.out.println("Cliente não possui saldo suficiente para realizar a compra de $" + valorCompra);
                }
            }
        } catch (InterruptedException | Conta.SaldoInsuficienteException e) {
            e.printStackTrace();
        }
    }
}

class Funcionario extends Thread {
    private Conta salario;
    private Conta investimento;

    public Funcionario(Conta salario, Conta investimento) {
        this.salario = salario;
        this.investimento = investimento;
    }

    public void run() {
        while (true) {
            salario.depositar(1400); // Recebe o salário
            double valorInvestimento = 1400 * 0.2;
            try {
                salario.sacar(valorInvestimento); // Investe 20% do salário
                investimento.depositar(valorInvestimento);
            } catch (Conta.SaldoInsuficienteException e) {
                e.printStackTrace();
            }
        }
    }
}

class Loja {
    private Conta conta;
    private Funcionario[] funcionarios;

    public Loja(Conta conta, int numFuncionarios) {
        this.conta = conta;
        this.funcionarios = new Funcionario[numFuncionarios];
        for (int i = 0; i < numFuncionarios; i++) {
            funcionarios[i] = new Funcionario(conta, new Conta(0)); // Cada funcionário tem uma conta de investimento
        }
    }

    public void pagarSalarios() {
        // Paga salários para todos os funcionários
        for (Funcionario funcionario : funcionarios) {
            funcionario.start();
        }
    }
}

class Banco {
    private final Lock lock = new ReentrantLock();

    public void transferencia(Conta origem, Conta destino, double valor) throws Conta.SaldoInsuficienteException {
        lock.lock();
        try {
            origem.sacar(valor);
            destino.depositar(valor);
        } finally {
            lock.unlock();
        }
    }
}

class SistemaBancario {
    public static void main(String[] args) {
        Conta contaBanco = new Conta(0);
        Banco banco = new Banco();

        Conta contaLoja1 = new Conta(0);
        Conta contaLoja2 = new Conta(0);

        Loja loja1 = new Loja(contaLoja1, 2);
        Loja loja2 = new Loja(contaLoja2, 2);

        // Inicializa as threads dos clientes com saldo inicial de 1000
        for (int i = 0; i < 5; i++) {
            Cliente cliente = new Cliente(new Conta(1000)); // Inicializa a conta do cliente com 1000
            cliente.start();
        }

        // Simulação de transações entre lojas e banco
        while (true) {
            try {
                banco.transferencia(contaLoja1, contaBanco, 1400); // Transfere o valor do salário da loja 1 para o banco
                contaLoja1.sacar(1400); // Paga os salários dos funcionários da loja 1
                banco.transferencia(contaLoja2, contaBanco, 1400); // Transfere o valor do salário da loja 2 para o banco
                contaLoja2.sacar(1400); // Paga os salários dos funcionários da loja 2
            } catch (Conta.SaldoInsuficienteException e) {
                e.printStackTrace();
            }
        }
    }
}



