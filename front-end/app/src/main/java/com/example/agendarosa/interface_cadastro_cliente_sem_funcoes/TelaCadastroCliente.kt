package com.example.agendarosa.interface_cadastro_cliente_sem_funcoes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agendarosa.ui.theme.*

/**
 * Interface Gráfica da Tela de Cadastro do Cliente (Sem integração com API)
 *
 * Estilos baseados no CSS oficial do Agenda Rosa (base-cadastro.css):
 * - Fundo gradiente (#fff7fa -> #f8d6e1)
 * - Card central com cantos arredondados (15dp)
 * - Campos de texto: Nome Completo, E-mail e Senha (com caracteres ocultados)
 * - Botão grande de Criar Conta na cor #e91e63
 */
@Composable
fun TelaCadastroCliente() {
    // Estados visuais dos campos
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }

    // Fundo gradiente idêntico ao CSS do Agenda Rosa
    val fundoGradiente = Brush.linearGradient(
        colors = listOf(FundoGradienteInicio, FundoGradienteFim)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fundoGradiente)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Título Agenda Rosa
                Text(
                    text = "Agenda Rosa",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = RosaBotao
                )

                // Subtítulo
                Text(
                    text = "Preencha os dados para criar a sua conta",
                    fontSize = 14.sp,
                    color = TextoMedio,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // 1. Campo Nome Completo
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome completo") },
                    placeholder = { Text("Digite o seu nome completo") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RosaBotao,
                        focusedLabelColor = RosaBotao,
                        cursorColor = RosaBotao,
                        unfocusedBorderColor = BordaInputPadrao
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Campo E-mail
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    placeholder = { Text("seuemail@dominio.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RosaBotao,
                        focusedLabelColor = RosaBotao,
                        cursorColor = RosaBotao,
                        unfocusedBorderColor = BordaInputPadrao
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Campo Senha (oculto por padrão com bolinhas/asteriscos)
                OutlinedTextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha") },
                    placeholder = { Text("Crie sua senha") },
                    singleLine = true,
                    visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                            Icon(
                                imageVector = if (mostrarSenha) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Mostrar/Ocultar Senha",
                                tint = RosaBotao
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RosaBotao,
                        focusedLabelColor = RosaBotao,
                        cursorColor = RosaBotao,
                        unfocusedBorderColor = BordaInputPadrao
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Botão Grande "Criar Conta"
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RosaBotao,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Criar Conta",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Link para Login
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Já possui conta? ",
                        fontSize = 13.sp,
                        color = TextoMedio
                    )
                    TextButton(
                        onClick = { },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Faça login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RosaLink
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaCadastroClientePreview() {
    AgendaRosaTheme {
        TelaCadastroCliente()
    }
}
