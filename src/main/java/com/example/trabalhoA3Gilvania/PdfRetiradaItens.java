package com.example.trabalhoA3Gilvania;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Gerador de PDF para retirada de itens (iText 7).
 * Correções: protege contra Strings nulas ao criar Paragraphs e adiciona logs de depuração
 * para ajudar a identificar por que um arquivo poderia ficar em branco.
 */
public class PdfRetiradaItens {

    // --- Definição de Cores ---
    public static final Color COR_TEXTO_PRINCIPAL = DeviceRgb.BLACK;
    public static final Color COR_TEXTO_SECUNDARIO = new DeviceRgb(100, 100, 100);
    public static final Color COR_BORDA_LEVE = new DeviceRgb(220, 220, 220);

    /**
     * Gera o PDF em disco no caminho 'dest'.
     * Lança IOException para que o chamador possa tratar falhas (e exibir alerta).
     */
    public static void gerarPdf(String dest,
                                String numeroRegistro,
                                String dataRetirada,
                                String representante,
                                String departamento,
                                List<Item> itens,
                                String assinatura1,
                                String assinatura2) throws IOException {
        // Normaliza parâmetros para evitar nulls
        numeroRegistro = numeroRegistro == null ? "" : numeroRegistro;
        dataRetirada = dataRetirada == null ? "" : dataRetirada;
        representante = representante == null ? "" : representante;
        departamento = departamento == null ? "" : departamento;
        assinatura1 = assinatura1 == null ? "" : assinatura1;
        assinatura2 = assinatura2 == null ? "" : assinatura2;

        System.out.println("[PdfRetiradaItens] gerarPdf chamado");
        System.out.println("  dest=" + dest);
        System.out.println("  numeroRegistro=" + numeroRegistro);
        System.out.println("  dataRetirada=" + dataRetirada);
        System.out.println("  representante=" + representante);
        System.out.println("  departamento=" + departamento);
        System.out.println("  assinatura1=" + (assinatura1.isEmpty() ? "<vazio>" : assinatura1));
        System.out.println("  assinatura2=" + (assinatura2.isEmpty() ? "<vazio>" : assinatura2));
        System.out.println("  itens == null ? " + (itens == null));
        System.out.println("  itensCount = " + (itens == null ? 0 : itens.size()));
        if (itens != null) {
            for (int i = 0; i < itens.size(); i++) {
                Item it = itens.get(i);
                System.out.printf("    item[%d] os=%s op=%s cod=%s qtdSol=%s qtdRet=%s solicit=%s%n",
                        i, safe(it.getOs()), safe(it.getOperacao()), safe(it.getCodItem()),
                        safe(it.getQtdSol()), safe(it.getQtdRet()), safe(it.getSolicitacao()));
            }
        }

        // Cria o documento usando try-with-resources (fecha automaticamente)
        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
             Document document = new Document(pdfDoc, PageSize.A4)) {

            document.setMargins(35, 35, 35, 35);

            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            criarCabecalho(document, fontBold, numeroRegistro);
            criarInfoSolicitante(document, fontRegular, fontBold, dataRetirada, representante, departamento);
            criarTabelaItens(document, fontRegular, fontBold, itens);
            criarObservacoes(document, fontRegular, fontBold);
            criarRodapeAssinatura(document, fontRegular, assinatura1, assinatura2);

            // document é fechado automaticamente pelo try-with-resources
            System.out.println("[PdfRetiradaItens] document.close() concluído");
        } catch (Exception e) {
            System.err.println("[PdfRetiradaItens] Erro durante gerarPdf: " + e.getMessage());
            e.printStackTrace();
            // se o arquivo foi criado mas está vazio, informamos o tamanho no console
            try {
                File f = new File(dest);
                System.out.println("[PdfRetiradaItens] arquivo exists=" + f.exists() + ", size=" + (f.exists() ? f.length() : 0));
            } catch (Exception ex) {
                // ignore
            }
            throw e instanceof IOException ? (IOException) e : new IOException(e);
        }

        // após fechar o document, imprimir info do arquivo
        try {
            File f = new File(dest);
            System.out.println("[PdfRetiradaItens] arquivo final criado exists=" + f.exists() + ", size=" + (f.exists() ? f.length() : 0));
        } catch (Exception ex) {
            // ignore
        }
    }

    private static void criarCabecalho(Document document, PdfFont fontBold,
                                       String numeroRegistro) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        // Logo
        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(10)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Carrega a imagem do classpath (/imagens/logo.png)
        try (InputStream is = PdfRetiradaItens.class.getResourceAsStream("/imagens/logo.png")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                ImageData imageData = ImageDataFactory.create(bytes);
                Image logo = new Image(imageData);
                logo.scaleToFit(45, 45);
                logoCell.add(logo);
            } else {
                // recurso não encontrado: placeholder em branco
                logoCell.add(new Paragraph(" "));
            }
        } catch (Exception e) {
            // se houver problema ao carregar a imagem, não interrompe a geração do PDF
            logoCell.add(new Paragraph(" "));
            System.err.println("Aviso: falha ao carregar logo: " + e.getMessage());
        }

        table.addCell(logoCell);

        Cell cellTitulo = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingLeft(10)
                .setPaddingBottom(2);

        // Título e subtítulo fixos
        cellTitulo.add(new Paragraph("FORMULÁRIO DE RETIRADA")
                .setFont(fontBold)
                .setFontSize(17)
                .setFontColor(COR_TEXTO_PRINCIPAL)
                .setTextAlignment(TextAlignment.RIGHT));

        cellTitulo.add(new Paragraph("Controle de Itens entregues a oficina")
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(COR_TEXTO_SECUNDARIO)
                .setTextAlignment(TextAlignment.RIGHT));

        // Registro parametrizado
        cellTitulo.add(new Paragraph("Registro Nº: " + safe(numeroRegistro))
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(COR_TEXTO_SECUNDARIO)
                .setTextAlignment(TextAlignment.RIGHT));

        table.addCell(cellTitulo);

        document.add(table);
        document.add(new LineSeparator(new SolidLine(0.5f))
                .setStrokeColor(COR_BORDA_LEVE)
                .setMarginTop(5));
    }

    private static void criarInfoSolicitante(Document document, PdfFont fontRegular, PdfFont fontBold,
                                             String dataRetirada, String representante, String departamento) {
        Paragraph pInfo = new Paragraph()
                .setFont(fontRegular)
                .setFontSize(11)
                .setMarginTop(15)
                .setMarginBottom(20)
                .setMultipliedLeading(1.4f);

        pInfo.add(new Text("Data da Retirada: ").setFont(fontBold));
        pInfo.add(safe(dataRetirada) + "\n");

        pInfo.add(new Text("Representante: ").setFont(fontBold));
        pInfo.add(safe(representante) + "\n");

        pInfo.add(new Text("Departamento: ").setFont(fontBold));
        pInfo.add(safe(departamento) + "\n");

        document.add(pInfo);
    }

    private static void criarTabelaItens(Document document, PdfFont fontRegular, PdfFont fontBold, List<Item> itens) {
        float[] columnWidths = {1, 1, 2, 5, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
        table.setMarginBottom(30);

        table.addHeaderCell(createHeaderCell("OS", fontBold));
        table.addHeaderCell(createHeaderCell("Operação", fontBold));
        table.addHeaderCell(createHeaderCell("Cod. Item", fontBold));
        table.addHeaderCell(createHeaderCell("Descrição", fontBold));
        table.addHeaderCell(createHeaderCell("Qtd. Sol.", fontBold));
        table.addHeaderCell(createHeaderCell("Qtd. Ret.", fontBold));
        table.addHeaderCell(createHeaderCell("Solicitado por:", fontBold));

        if (itens != null && !itens.isEmpty()) {
            for (Item item : itens) {
                table.addCell(createDataCell(safe(item.getOs()), fontRegular, TextAlignment.CENTER));
                table.addCell(createDataCell(safe(item.getOperacao()), fontRegular, TextAlignment.CENTER));
                table.addCell(createDataCell(safe(item.getCodItem()), fontRegular, TextAlignment.CENTER));
                table.addCell(createDataCell(safe(item.getDescricao()), fontRegular, TextAlignment.LEFT));
                table.addCell(createDataCell(safe(item.getQtdSol()), fontRegular, TextAlignment.CENTER));
                table.addCell(createDataCell(safe(item.getQtdRet()), fontRegular, TextAlignment.CENTER));
                table.addCell(createDataCell(safe(item.getSolicitacao()), fontRegular, TextAlignment.CENTER));
            }

            document.add(table);
        } else {
            // adiciona um parágrafo informando que não há itens (útil para depuração)
            document.add(new Paragraph("Nenhum item para exibir").setFont(fontRegular).setFontSize(10).setFontColor(COR_TEXTO_SECUNDARIO));
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static Cell createHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(safe(text)))
                .setFont(font)
                .setFontSize(7.5f)
                .setFontColor(COR_TEXTO_PRINCIPAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COR_TEXTO_PRINCIPAL, 1.5f));
    }

    private static Cell createDataCell(String text, PdfFont font, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(safe(text)))
                .setFont(font)
                .setFontSize(6.7f)
                .setTextAlignment(alignment)
                .setPaddingTop(3)
                .setPaddingBottom(3)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COR_BORDA_LEVE, 1f));
    }

    private static void criarObservacoes(Document document, PdfFont fontRegular, PdfFont fontBold) {
        document.add(new Paragraph("Observações:")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginTop(10));

        Div obsDiv = new Div()
                .setBorder(new SolidBorder(COR_BORDA_LEVE, 1f))
                .setBackgroundColor(DeviceRgb.WHITE)
                .setPadding(12)
                .setMarginBottom(20)
                .setMinHeight(80);

        obsDiv.add(new Paragraph("\u00A0").setFontSize(2).setMargin(0));
        document.add(obsDiv);
    }

    private static void criarRodapeAssinatura(Document document, PdfFont fontRegular,
                                              String assinatura1, String assinatura2) {
        assinatura1 = safe(assinatura1);
        assinatura2 = safe(assinatura2);

        Table tableAssinaturas = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginTop(60);

        Cell cellSol = new Cell().setBorder(Border.NO_BORDER).setPadding(10).setPaddingLeft(20).setPaddingRight(20);
        cellSol.add(new LineSeparator(new SolidLine(0.5f)).setStrokeColor(COR_TEXTO_SECUNDARIO));
        cellSol.add(new Paragraph(assinatura1).setFont(fontRegular).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginTop(5));
        cellSol.add(new Paragraph("(Assinatura Almoxarifado)")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(COR_TEXTO_SECUNDARIO)
                .setTextAlignment(TextAlignment.CENTER));
        tableAssinaturas.addCell(cellSol);

        Cell cellEst = new Cell().setBorder(Border.NO_BORDER).setPadding(10).setPaddingLeft(20).setPaddingRight(20);
        cellEst.add(new LineSeparator(new SolidLine(0.5f)).setStrokeColor(COR_TEXTO_SECUNDARIO));
        cellEst.add(new Paragraph(assinatura2).setFont(fontRegular).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginTop(5));
        cellEst.add(new Paragraph("(Assinatura responsável pela retirada)")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(COR_TEXTO_SECUNDARIO)
                .setTextAlignment(TextAlignment.CENTER));
        tableAssinaturas.addCell(cellEst);

        document.add(tableAssinaturas);
    }

    // --- Classe auxiliar para representar os itens ---
    public static class Item {
        private final String os;
        private final String operacao;
        private final String codItem;
        private final String descricao;
        private final String qtdSol;
        private final String qtdRet;
        private final String solicitacao;

        public Item(String os, String operacao, String codItem, String descricao,
                    String qtdSol, String qtdRet, String solicitacao) {
            this.os = os;
            this.operacao = operacao;
            this.codItem = codItem;
            this.descricao = descricao;
            this.qtdSol = qtdSol;
            this.qtdRet = qtdRet;
            this.solicitacao = solicitacao;
        }

        public String getOs() { return os; }
        public String getOperacao() { return operacao; }
        public String getCodItem() { return codItem; }
        public String getDescricao() { return descricao; }
        public String getQtdSol() { return qtdSol; }
        public String getQtdRet() { return qtdRet; }
        public String getSolicitacao() { return solicitacao; }
    }
}