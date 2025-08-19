// Espera a que todo el contenido del DOM esté cargado antes de ejecutar el script
document.addEventListener('DOMContentLoaded', () => {

    // --- MODELO DE DATOS ---
    // Almacena los precios y los totales de ventas.
    // Separar esto del resto del código facilita su actualización.
    const state = {
        precios: {
            '95': 1.50, // Súper
            '91': 1.40, // Regular
            'diesel': 1.25
        },
        ventas: [],
        totales: {
            '95': 0,
            '91': 0,
            'diesel': 0
        }
    };

    // --- REFERENCIAS A ELEMENTOS DEL DOM ---
    // Guardamos los elementos que vamos a manipular para no tener que buscarlos cada vez.
    const form = document.getElementById('gas-form');
    const tipoGasolinaSelect = document.getElementById('tipo-gasolina');
    const cantidadLitrosInput = document.getElementById('cantidad-litros');
    const pagaConInput = document.getElementById('paga-con');
    const tablaVentasBody = document.getElementById('tabla-ventas-body');
    const totalSuperDisplay = document.getElementById('total-super');
    const totalRegularDisplay = document.getElementById('total-regular');
    const totalDieselDisplay = document.getElementById('total-diesel');
    const modal = document.getElementById('modal-vuelto');
    const modalTotal = document.getElementById('modal-total');
    const modalCambio = document.getElementById('modal-cambio');
    const cerrarModalBtn = document.getElementById('cerrar-modal');

    // --- LÓGICA DE LA APLICACIÓN ---

    /**
     * Procesa el envío del formulario de venta.
     * @param {Event} event - El evento de envío del formulario.
     */
    function handleVenta(event) {
        event.preventDefault(); // Evita que la página se recargue

        // 1. Obtener y validar los datos del formulario
        const tipo = tipoGasolinaSelect.value;
        const cantidad = parseFloat(cantidadLitrosInput.value);
        const pagaCon = parseFloat(pagaConInput.value) || 0; // Si está vacío, es 0

        if (isNaN(cantidad) || cantidad <= 0) {
            alert('Por favor, ingrese una cantidad válida de litros.');
            return;
        }

        // 2. Calcular el total de la venta
        const precioPorLitro = state.precios[tipo];
        const totalVenta = cantidad * precioPorLitro;

        if (pagaCon > 0 && pagaCon < totalVenta) {
            alert('El monto de pago es insuficiente.');
            return;
        }

        // 3. Crear un registro de la venta
        const nuevaVenta = {
            id: Date.now(), // ID único basado en la fecha
            tipo,
            cantidad,
            precioPorLitro,
            totalVenta,
            fecha: new Date()
        };

        // 4. Actualizar el estado de la aplicación
        state.ventas.push(nuevaVenta);
        state.totales[tipo] += cantidad;

        // 5. Actualizar la interfaz de usuario
        render();

        // 6. Mostrar el modal con el vuelto si aplica
        if (pagaCon > 0) {
            const cambio = pagaCon - totalVenta;
            mostrarModalVuelto(totalVenta, cambio);
        }

        // 7. Limpiar el formulario para la siguiente venta
        form.reset();
    }

    /**
     * Muestra el modal con la información del total y el cambio.
     * @param {number} total - El total de la venta.
     * @param {number} cambio - El cambio a devolver al cliente.
     */
    function mostrarModalVuelto(total, cambio) {
        modalTotal.textContent = `$${total.toFixed(2)}`;
        modalCambio.textContent = `$${cambio.toFixed(2)}`;
        modal.classList.remove('hidden');
        // Pequeño delay para permitir que la transición CSS se active
        setTimeout(() => {
            modal.querySelector('.transform').classList.add('show');
        }, 10);
    }

    /**
     * Oculta el modal de vuelto.
     */
    function cerrarModal() {
        modal.querySelector('.transform').classList.remove('show');
        setTimeout(() => {
            modal.classList.add('hidden');
        }, 300); // Espera a que termine la animación de salida
    }

    // --- FUNCIONES DE RENDERIZADO ---
    // Estas funciones se encargan de dibujar los datos en la pantalla.

    /**
     * Renderiza (dibuja) toda la tabla de ventas a partir del estado actual.
     */
    function renderTabla() {
        tablaVentasBody.innerHTML = ''; // Limpia la tabla antes de redibujar

        if (state.ventas.length === 0) {
            tablaVentasBody.innerHTML = `
                <tr class="border-b border-slate-700">
                    <td colspan="5" class="text-center px-6 py-8 text-slate-500">No hay ventas registradas todavía.</td>
                </tr>
            `;
            return;
        }

        // Crea una fila por cada venta, comenzando por la más reciente
        state.ventas.slice().reverse().forEach(venta => {
            const tr = document.createElement('tr');
            tr.className = 'bg-slate-800/50 border-b border-slate-700 hover:bg-slate-700/70 transition-colors';
            
            const tipoGasolinaNombres = {
                '95': 'Súper 95',
                '91': 'Regular 91',
                'diesel': 'Diesel'
            };

            tr.innerHTML = `
                <td class="px-6 py-4 font-medium text-white">${tipoGasolinaNombres[venta.tipo]}</td>
                <td class="px-6 py-4">${venta.cantidad.toFixed(2)} L</td>
                <td class="px-6 py-4">$${venta.precioPorLitro.toFixed(2)}</td>
                <td class="px-6 py-4 font-bold text-cyan-400">$${venta.totalVenta.toFixed(2)}</td>
                <td class="px-6 py-4">${venta.fecha.toLocaleString()}</td>
            `;
            tablaVentasBody.appendChild(tr);
        });
    }

    /**
     * Actualiza los contadores totales en la sección de resumen.
     */
    function renderTotales() {
        totalSuperDisplay.textContent = `${state.totales['95'].toFixed(2)} L`;
        totalRegularDisplay.textContent = `${state.totales['91'].toFixed(2)} L`;
        totalDieselDisplay.textContent = `${state.totales['diesel'].toFixed(2)} L`;
    }

    /**
     * Función principal que llama a todas las funciones de renderizado.
     * Mantiene la UI sincronizada con el estado de los datos.
     */
    function render() {
        renderTabla();
        renderTotales();
    }

    // --- INICIALIZACIÓN Y EVENT LISTENERS ---

    // Asigna las funciones a los eventos correspondientes.
    form.addEventListener('submit', handleVenta);
    cerrarModalBtn.addEventListener('click', cerrarModal);
    // Permite cerrar el modal haciendo clic fuera de él
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            cerrarModal();
        }
    });

    // Renderiza el estado inicial de la aplicación al cargar la página.
    render();
});